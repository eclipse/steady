package promote

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/internal/isolate"
)

func checkPrereqs() string {
	path, err := exec.LookPath("helm")
	if err != nil {
		log.Fatal(err)
	}
	fmt.Printf("helm binary found on path %s\n", path)
	result, _ := exec.Command("helm", "version", "--short").Output()
	fmt.Printf("current helm version %s\n", string(result))
	if !strings.Contains(string(result), "v3") {
		log.Fatal("requires helm 3")
	}

	return path
}

var (
	path = checkPrereqs()
)

func checkErr(result []byte) bool {
	return strings.Contains(strings.ToUpper(string(result)), "ERROR")
}

func helmLint(ChartDir string) {
	fmt.Println("Checking if new chart is valid")
	result, _ := exec.Command("helm3", "lint", ChartDir).Output()
	if checkErr(result) {
		log.Fatal(string(result))
	}
}

func helmList(release string, namespace string) error {
	fmt.Println("Checking for given helm release existence")
	result, _ := exec.Command("helm3", "ls", "--short", "--namespace", namespace).Output()
	if checkErr(result) {
		log.Fatal(string(result))
	}
	if !strings.Contains(string(result), release) {
		log.Fatalf("Did not find given release %s in namespace %s", release, namespace)
	}
	fmt.Printf("Found charts %s\n", result)
	return nil
}

// HelmUpgrade performs the upgrade with schema migration
func HelmUpgrade(context *Context) error {
	if err := os.Chdir(context.ChartDir); err != nil {
		return err
	}

	helmLint(".")
	if listErr := helmList(context.OldRelease, context.CoreNamespace); listErr != nil {
		return listErr
	}

	statefulsetName := fmt.Sprintf("%s-database-slave", context.OldRelease)
	claimName := isolate.Isolate(&statefulsetName, &context.CoreNamespace)
	if claimName != nil {
		replaceFiles(context, *claimName)
		fmt.Printf("Installing new release %s\n", context.NewRelease)
		result, _ := exec.Command("helm3", "install", context.NewRelease, ".").Output()
		if checkErr(result) {
			log.Fatal(string(result))
		}
	} else {
		return fmt.Errorf("Encountered unknown error with fetching claimName")
	}

	return nil
}
