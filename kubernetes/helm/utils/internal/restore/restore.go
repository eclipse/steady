package restore

import (
	"bytes"
	"fmt"
	"io"
	"log"

	"time"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/connect"
	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/convert"
	batchv1 "k8s.io/api/batch/v1"
	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"k8s.io/client-go/kubernetes"
)

// Context passed for the restore command
type Context struct {
	Kubeconfig  string         `yaml:"kubeconfig"`
	Source      DatabaseAccess `yaml:"source,omitempty"`
	Destination DatabaseAccess `yaml:"destination,omitempty"`
	Namespace   string         `yaml:"namespace"`
	Purge       bool           `yaml:"purge"`
}

// DatabaseAccess to model the source and destination accesses
type DatabaseAccess struct {
	Host     string `yaml:"host"`
	Port     string `yaml:"port"`
	User     string `yaml:"user"`
	Password string `yaml:"password"`
	Path     string `yaml:"path"`
	Database string `yaml:"db"`
}

func getClient() *kubernetes.Clientset {
	clientset, clientErr := connect.GetClient("")
	if clientErr != nil {
		log.Fatal(clientErr)
	}
	return clientset
}

var (
	clientset = getClient()
)

func createJob(context *Context) *batchv1.Job {
	return &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name: "database-migrator",
		},
		Spec: batchv1.JobSpec{
			Parallelism:  convert.Int32Ptr(1),
			BackoffLimit: convert.Int32Ptr(0),

			Template: apiv1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: map[string]string{
						"app.kubernetes.io/name": "database-migrator",
					},
				},
				Spec: apiv1.PodSpec{
					RestartPolicy: "Never",
					Containers: []apiv1.Container{
						{
							Name:  "database-migrator-container",
							Image: "postgres:11.5-alpine",
							Command: []string{
								"sh",
								"-c",
								`
	#!/bin/bash
	DUMP_URL="${SOURCE_HOST}:${SOURCE_PORT}${SOURCE_PATH}"
	DROP_CMD="'DROP SCHEMA public CASCADE; CREATE SCHEMA public;'"

	echo "Installing prerequisites { curl }"
	apk add --no-cache curl

	START_TIME=$(date +%s)
	curl -kso /tmp/tmp.dump -u $SOURCE_USER:$SOURCE_PASSWORD $DUMP_URL

	if [ $? -ne 0 ]; then
	    echo "[-] Could not fetch any $s dump. Exiting"
	    exit 1
	fi

	echo "[+] Fetched in $((($(date +%s)-$START_TIME))) seconds."
	START_TIME=$(date +%s)

	if [ $PURGE ]; then
	  sh -c "echo $DROP_CMD | psql --host $DESTINATION_HOST -U $DESTINATION_USER --dbname $DESTINATION_DB"
	fi

	pg_restore --verbose --clean --if-exists --no-acl --host $DESTINATION_HOST -U $DESTINATION_USER --dbname $DESTINATION_DB /tmp/tmp.dump
	echo "[+] Restored in $((($(date +%s)-$START_TIME))) seconds."
	exit 0
	                `,
							},
							Env: []apiv1.EnvVar{
								{
									Name:  "SOURCE_HOST",
									Value: context.Source.Host,
								},
								{
									Name:  "SOURCE_PORT",
									Value: string(context.Source.Port),
								},
								{
									Name:  "SOURCE_PATH",
									Value: context.Source.Path,
								},
								{
									Name:  "SOURCE_USER",
									Value: context.Source.User,
								},
								{
									Name:  "SOURCE_PASSWORD",
									Value: context.Source.Password,
								},
								{
									Name:  "DESTINATION_HOST",
									Value: context.Destination.Host,
								},
								{
									Name:  "DESTINATION_USER",
									Value: context.Destination.User,
								},
								{
									Name:  "DESTINATION_PASSWORD",
									Value: context.Destination.Password,
								},
								{
									Name:  "DESTINATION_DB",
									Value: context.Destination.Database,
								},
								{
									Name:  "PGPASSWORD",
									Value: context.Destination.Password,
								},
							},
						},
					},
				},
			},
		},
	}
}

func fetchJobPods(label *string, namespace *string) (*apiv1.Pod, error) {
	podClient := clientset.CoreV1().Pods(*namespace)
	list, err := podClient.List(metav1.ListOptions{
		LabelSelector: *label,
	})

	if err != nil {
		return nil, err
	}

	// Should launch only a single job
	for _, pod := range list.Items {
		return &pod, nil
	}
	return nil, fmt.Errorf("restore.fetchJobPods: No pod has been launched")
}

func getPodLogs(pod apiv1.Pod) (string, error) {
	podLogOpts := apiv1.PodLogOptions{
		SinceSeconds: convert.Int64Ptr(2),
	}
	req := clientset.CoreV1().Pods(pod.Namespace).GetLogs(pod.Name, &podLogOpts)
	podLogs, err := req.Stream()
	if err != nil {
		return "", fmt.Errorf("restore.getPodLogs : error in opening stream")
	}
	defer podLogs.Close()

	buf := new(bytes.Buffer)
	_, err = io.Copy(buf, podLogs)
	if err != nil {
		return "", fmt.Errorf("restore.getPodLogs : error in copy information from podLogs to buf")
	}
	str := buf.String()

	return str, nil
}

// LoadDumps loads a dump according to the context passed
func LoadDumps(context *Context) {
	jobClient := clientset.BatchV1().Jobs(context.Namespace)
	job := createJob(context)

	_, err := jobClient.Create(job)
	if err != nil {
		log.Fatal(err)
	}

	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("Migration launched")
	// Get pods associated with this job
	labelSelector := fmt.Sprintf("app.kubernetes.io/name=database-migrator")

	fmt.Println("Sleeping for 10s to wait for pod to spin up")
	time.Sleep(10000 * time.Millisecond)
	pod, fetchErr := fetchJobPods(&labelSelector, &context.Namespace)
	if fetchErr != nil {
		log.Fatal(fetchErr)
	}

	fmt.Printf("Log tailing started on pod %s\n", pod.Name)
	for {
		time.Sleep(2000 * time.Millisecond)
		podLog, podLogerr := getPodLogs(*pod)
		if podLogerr != nil {
			cleanUpJob(job, &context.Namespace)
		}
		if podLog != "" {
			fmt.Println(podLog)
		}
	}
}

func cleanUpJob(job *batchv1.Job, namespace *string) {
	jobClient := clientset.BatchV1().Jobs(*namespace)
	deleteErr := jobClient.Delete(job.Name, &metav1.DeleteOptions{})
	if deleteErr != nil {
		log.Fatal(deleteErr)
	}
	fmt.Printf("Successfully delete job...")
}
