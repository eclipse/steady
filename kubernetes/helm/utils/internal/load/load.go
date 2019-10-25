// Package load provides multithreading job and config creation that uses
// patchanalyzer to load vulnerabilities into the vulnerability database
package load

import (
	"fmt"
	"io/ioutil"
	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	corev1 "k8s.io/client-go/kubernetes/typed/core/v1"
	"log"
	"os"
	"strconv"
	"strings"
	"sync"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/connect"
	"gopkg.in/yaml.v3"
	watchapi "k8s.io/apimachinery/pkg/watch"
)

// CVE represents a simplified vulnerability to be loaded
type CVE struct {
	Reference   string `yaml:"reference"`
	Repo        string `yaml:"repo"`
	Commit      string `yaml:"commit"`
	Description string `yaml:"description"`
	Links       string `yaml:"links"`
}

// Context represents the Context passed to execute the loading job
type Context struct {
	KubeConfig  string `yaml:"kubeconfig"`
	Concurrent  int    `yaml:"concurrent"`
	ReleaseName string `yaml:"release"`
	Namespace   string `yaml:"namespace"`
	Skip        bool   `yaml:"skip"`
	DryRun      bool   `yaml:"dryrun"`
	Source      string `yaml:"source"`
}

func getCVEList(source *string) ([]CVE, error) {
	if _, err := os.Stat(*source); err != nil {
		return nil, err
	}
	yamlFile, readErr := ioutil.ReadFile(*source)
	if readErr != nil {
		return nil, readErr
	}

	cveList := make(map[string][]CVE)
	if unmarshallErr := yaml.Unmarshal(yamlFile, cveList); unmarshallErr != nil {
		fmt.Println(unmarshallErr)
		return nil, unmarshallErr
	}
	if cveList["bugs"] != nil {
		return cveList["bugs"], nil
	}

	return nil, fmt.Errorf("Malformed source file")
}

// SplitCVE splits loaded bugs into equal chunks to be distributed
func SplitCVE(context *Context) ([][]CVE, error) {
	cveList, err := getCVEList(&context.Source)
	if err != nil {
		return nil, err
	}

	var distributedCve [][]CVE
	chunkSize := (len(cveList) + context.Concurrent - 1) / context.Concurrent

	for i := 0; i < len(cveList); i += chunkSize {
		end := i + chunkSize
		if end > len(cveList) {
			end = len(cveList)
		}

		distributedCve = append(distributedCve, cveList[i:end])
	}

	return distributedCve, nil
}

// UploadBugs helps upload the bugs into the desired restbackend
func UploadBugs(context *Context, bugs [][]CVE) {
	var wg sync.WaitGroup

	clientset, connectErr := connect.GetClient(context.KubeConfig)
	if connectErr != nil {
		log.Fatal(connectErr)
	}
	podClient := clientset.CoreV1().Pods(context.Namespace)

	for chunkID, cveList := range bugs {
		wg.Add(1)
		go func(context *Context, podClient corev1.PodInterface, bugs []CVE, chunkID int) {
			defer wg.Done()
			failed := []CVE{}
			bugLength := len(bugs)

			for progress, bug := range bugs {
				pod := createPod(podClient, chunkID, bug, *context)
				if _, err := podClient.Create(&pod); err != nil {
					fmt.Printf("Chunk %d [%d/%d]: Pod creation failed %s\n", chunkID, progress+1, bugLength, err)
				} else {
					fmt.Printf("Chunk %d [%d/%d]: Pod to analyze bug %s started \n", chunkID, progress+1, bugLength, bug.Reference)

					// Watches pod for completion
					watch, _ := podClient.Watch(metav1.ListOptions{
						LabelSelector: fmt.Sprintf("app.kubernetes.io/name=%s,app.kubernetes.io/instance=%s", getPodName(bug), strconv.Itoa(chunkID)),
					})

					for event := range watch.ResultChan() {
						podStatus, ok := event.Object.(*apiv1.Pod)

						if !ok {
							log.Fatalf("Chunk %d [%d/%d]: Encountered unknown event type\n", progress+1, bugLength, chunkID)
							watch.Stop()
						}

						if event.Type == watchapi.Deleted {
							fmt.Printf("Chunk %d [%d/%d]: Bug analysis %s deleted by user, stopping execution\n", progress+1, bugLength, chunkID, bug.Reference)
							watch.Stop()
							wg.Done()
						}

						if podStatus.Status.Phase == apiv1.PodFailed || podStatus.Status.Phase == apiv1.PodUnknown {
							fmt.Printf("Chunk %d [%d/%d]: Bug analysis %s failed\n", chunkID, progress+1, bugLength, bug.Reference)
							failed = append(failed, bug)
							watch.Stop()
						}

						if podStatus.Status.Phase == apiv1.PodSucceeded {
							// Checks container status for proper exit code
							lastState := podStatus.Status.ContainerStatuses[0].State.Terminated

							// Error on code execution
							if lastState.ExitCode != 0 {
								fmt.Printf("Chunk %d [%d/%d]: Bug analysis %s failed (to see log use `kubectl get logs -n %s %s`)\n", chunkID, progress+1, bugLength, bug.Reference, context.Namespace, getPodName(bug))
								failed = append(failed, bug)
							} else {
								// Only succeeded containers are deleted
								if deleteErr := podClient.Delete(pod.Name, &metav1.DeleteOptions{}); err != nil {
									fmt.Printf("Chunk %d [%d/%d]: Bug analysis %s could not be delete because (%s)", chunkID, progress+1, bugLength, bug.Reference, deleteErr)
								}
								fmt.Printf("Chunk %d [%d/%d]: Bug analysis %s succeeded \n", chunkID, progress+1, bugLength, bug.Reference)
							}
							watch.Stop()
						}
					}
				}
			}
			fmt.Printf(`
---
Chunk %d: completed with %d failed analysis / %d bugs
---

`, chunkID, len(failed), bugLength)
		}(context, podClient, cveList, chunkID)
	}
	wg.Wait()
}

func getChunkName(chunkID int) string {
	return "bugs-loader-" + strconv.Itoa(chunkID)
}

func getPodName(bug CVE) string {
	return "bugs-loader-" + strings.ToLower(bug.Reference)
}

func getBackendService(release *string) string {
	return "http://" + *release + "-restbackend:8091/backend"
}

func createPod(podClient corev1.PodInterface, chunkID int, bug CVE, context Context) apiv1.Pod {
	pod := &apiv1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name: getPodName(bug),
			Labels: map[string]string{
				"app.kubernetes.io/name":     getPodName(bug),
				"app.kubernetes.io/part-of":  "bugs-loader",
				"app.kubernetes.io/instance": strconv.Itoa(chunkID),
			},
		},
		Spec: apiv1.PodSpec{
			RestartPolicy: "Never",
			Containers: []apiv1.Container{
				{
					Name:  getPodName(bug),
					Image: "ichbinfrog/patchanalyzer:v0.0.7",
					//Image:           "vulas/vulnerability-assessment-tool-patch-analyzer:3.1.7-SNAPSHOT",
					ImagePullPolicy: "Always",
					// Args: []string{
					// 	"com.vulas.sap.psr.vulas.patcha.PatchAnalyzer",
					// 	"-b",
					// 	bug.Reference,
					// 	"-r",
					// 	bug.Repo,
					// 	"-e",
					// 	bug.Commit,
					// 	"-descr",
					// 	strconv.Quote(bug.Description),
					// 	"-links",
					// 	strconv.Quote(bug.Links),
					// },
					Command: []string{
						"/bin/sh",
						"-c",
						"java -jar /vulas/patch-analyzer.jar com.sap.psr.vulas.PatchAnalyzer -r " + bug.Repo + " -b " + bug.Reference + " -e " + bug.Commit,
					},
					Env: []apiv1.EnvVar{
						{
							Name:  "vulas.shared.backend.serviceUrl",
							Value: getBackendService(&context.ReleaseName),
						},
					},
				},
			},
		},
	}

	if !context.DryRun {
		pod.Spec.Containers[0].Command[2] += " -u "
	}

	if context.Skip {
		pod.Spec.Containers[0].Command[2] += "-sie"
	}

	return *pod
}

// CleanLoad deletes all pods associated with the bug loader in the given namespace
func CleanLoad(context *Context) {
	clientset, connectErr := connect.GetClient(context.KubeConfig)
	if connectErr != nil {
		log.Fatal(connectErr)
	}
	podClient := clientset.CoreV1().Pods(context.Namespace)
	podList, listErr := podClient.List(metav1.ListOptions{
		LabelSelector: "app.kubernetes.io/part-of=bugs-loader",
	})

	if listErr != nil {
		log.Fatal(listErr)
	}

	for _, pod := range podList.Items {
		fmt.Printf("CLEANUP: Deleting pod %s\n", pod.Name)
		podClient.Delete(pod.Name, &metav1.DeleteOptions{})
	}
}
