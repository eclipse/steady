package isolate

import (
	"fmt"
	"log"
	"strings"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/connect"
	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/convert"
	v1 "k8s.io/api/apps/v1"
	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	interfacev1 "k8s.io/client-go/kubernetes/typed/apps/v1"
	"k8s.io/client-go/util/retry"
)

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

func getStatefulSets(name *string, namespace *string) (interfacev1.StatefulSetInterface, *v1.StatefulSet, error) {
	statefulsetClient := clientset.AppsV1().StatefulSets(*namespace)
	statefulset, getErr := statefulsetClient.Get(*name, metav1.GetOptions{})

	if getErr != nil {
		return nil, nil, getErr
	}

	return statefulsetClient, statefulset, nil
}

func getPVC(pod *apiv1.Pod) *string {
	if pod != nil {
		for _, volume := range pod.Spec.Volumes {
			if strings.Contains(volume.Name, "volume-claim") {
				claimName := volume.VolumeSource.PersistentVolumeClaim.ClaimName
				fmt.Printf("Reallocating PVC %s bound to pod %s\n", claimName, pod.ObjectMeta.Name)
				return &claimName
			}
		}
	}
	return nil
}

func getPods(name *string, namespace *string) (*apiv1.Pod, error) {
	podClient := clientset.CoreV1().Pods(*namespace)
	statefulSetSelector := fmt.Sprintf("app.kubernetes.io/instance=%s", *name)
	fmt.Printf("Constructing label selector %s\n", statefulSetSelector)

	podList, listErr := podClient.List(metav1.ListOptions{
		LabelSelector: statefulSetSelector,
		Limit:         100,
	})

	if listErr != nil {
		return nil, listErr
	}
	if len(podList.Items) != 0 {
		isolatedPod := podList.Items[len(podList.Items)-1]
		return &isolatedPod, nil
	}
	return nil, nil
}

func scaleDown(name *string, namespace *string) error {
	statefulsetClient, statefulset, getErr := getStatefulSets(name, namespace)

	if getErr != nil {
		return getErr
	}

	fmt.Printf("Found statefulset %s in namespace %s\n", *name, *namespace)
	if *statefulset.Spec.Replicas <= int32(1) {
		return (fmt.Errorf("Statefulset has %d replicas, which is not enough for migration (at least 2 required)", *statefulset.Spec.Replicas))
	}

	retryErr := retry.RetryOnConflict(retry.DefaultRetry, func() error {
		statefulset.Spec.Replicas = convert.Int32Ptr(*statefulset.Spec.Replicas - 1)
		_, updateErr := statefulsetClient.Update(statefulset)
		return updateErr
	})

	if retryErr != nil {
		return retryErr
	}
	fmt.Printf("Successfully scaled down replicas to %d\n", *statefulset.Spec.Replicas)
	return nil
}

// Isolate helps scale down replicas for the postgres slaves whilst also keeping
// old PersistentVolumeClaim for optimal migration speed
func Isolate(name *string, namespace *string) *string {
	pod, _ := getPods(name, namespace)
	if pod == nil {
		log.Fatalf("Did not find any matching master instance")
	}
	pvcName := getPVC(pod)

	if pvcName == nil {
		log.Fatalf("Failed to find PVC mounted to pod %s", *pvcName)
	}

	if scaleErr := scaleDown(name, namespace); scaleErr != nil {
		log.Fatal(scaleErr)
	}
	cleanUpJob(namespace, pvcName)
	return pvcName
}
