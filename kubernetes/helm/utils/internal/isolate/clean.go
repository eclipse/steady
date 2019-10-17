// Package isolate provides a module to isolate a pod from the vulnerability-assessment-tool-core
// chart into a new release in order to allow for a migration with schema changes
package isolate

import (
	"fmt"
	"log"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/convert"
	batchv1 "k8s.io/api/batch/v1"
	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func cleanUpJob(namespace *string, claimName *string) {
	name := "database-master-promotion"
	jobClient := clientset.BatchV1().Jobs(*namespace)

	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name: name,
		},
		Spec: batchv1.JobSpec{
			Parallelism:           convert.Int32Ptr(1),
			ActiveDeadlineSeconds: convert.Int64Ptr(100),
			BackoffLimit:          convert.Int32Ptr(0),

			Template: apiv1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: map[string]string{
						"app.kubernetes.io/name": name,
					},
				},
				Spec: apiv1.PodSpec{
					RestartPolicy: "Never",
					Volumes: []apiv1.Volume{
						{
							Name: *claimName,
							VolumeSource: apiv1.VolumeSource{
								PersistentVolumeClaim: &apiv1.PersistentVolumeClaimVolumeSource{
									ClaimName: *claimName,
								},
							},
						},
					},
					Containers: []apiv1.Container{
						{
							Name:  name,
							Image: "postgres:11.5-alpine",
							Command: []string{
								"sh",
								"-c",
								"rm -f /var/lib/postgresql/data/pgdata/recovery.conf",
							},
							VolumeMounts: []apiv1.VolumeMount{
								{
									Name:      *claimName,
									MountPath: "/var/lib/postgresql/data",
								},
							},
							SecurityContext: &apiv1.SecurityContext{
								RunAsUser: convert.Int64Ptr(999),
							},
						},
					},
				},
			},
		},
	}

	_, err := jobClient.Create(job)
	if err != nil {
		log.Fatal(err)
	}

	// Watch job progression through events
	labelSelector := fmt.Sprintf("app.kubernetes.io/name=%s", name)
	watcher, err := jobClient.Watch(metav1.ListOptions{
		LabelSelector: labelSelector,
	})

	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("Master promotion job launched, watching for changes in the cluster")
	jobChange := watcher.ResultChan()

	for event := range jobChange {
		fmt.Printf("Job %s: encountered event %v\n", job.Name, event.Type)
		jobWatch, ok := event.Object.(*batchv1.Job)
		if !ok {
			log.Fatal("Unexpected error")
		}

		status := jobWatch.Status
		if status.Succeeded >= 1 || status.Failed >= 1 || event.Type == "DELETED" {
			fmt.Println(status.Conditions)
			fmt.Printf("Deleting job...")
			deleteErr := jobClient.Delete(job.Name, &metav1.DeleteOptions{})

			if deleteErr != nil {
				log.Fatal(deleteErr)
			}
			fmt.Println("Successfully deleted job (Pod is maintained to allow for further log viewing)")
			return
		}
	}
}
