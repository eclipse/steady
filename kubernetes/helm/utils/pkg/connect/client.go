package connect

import (
	"path/filepath"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
)

// GetClient returns a kubernetes client built from a kubeconfig string
func GetClient(kubeconfig string) (*kubernetes.Clientset, error) {
	if kubeconfig == "" {
		if home := homedir.HomeDir(); home != "" {
			kubeconfig = filepath.Join(home, ".kube", "config")
		}
	}

	config, configErr := clientcmd.BuildConfigFromFlags("", kubeconfig)
	if configErr != nil {
		return nil, configErr
	}

	clientset, clientErr := kubernetes.NewForConfig(config)
	if clientErr != nil {
		return nil, clientErr
	}

	return clientset, nil
}
