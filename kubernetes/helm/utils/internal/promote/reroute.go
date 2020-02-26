package promote

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/exec"

	"gopkg.in/yaml.v3"
)

func alterWatch(src, dst, nextRelease string, overwrite bool) error {
	fmt.Printf("Rotating values from file %s to %s\n", src, dst)
	yamlFile, err := ioutil.ReadFile(src)
	if err != nil {
		return err
	}

	oldValues := make(map[string]interface{})
	unmarshalErr := yaml.Unmarshal(yamlFile, &oldValues)
	if unmarshalErr != nil {
		return unmarshalErr
	}

	if global := oldValues["global"]; global != nil {
		managedRelease := global.(map[string]interface{})["managedRelease"]
		if !overwrite && managedRelease != nil {
			return fmt.Errorf("promote.Copy : existing value cannot be overwritten")
		}
		global.(map[string]interface{})["managedRelease"] = nextRelease
	}

	newValues, marshalErr := yaml.Marshal(&oldValues)
	if marshalErr != nil {
		return marshalErr
	}

	if writeErr := ioutil.WriteFile(dst, newValues, 0644); writeErr != nil {
		return writeErr
	}
	return nil
}

// Reroute modifies the vulnerability-assessment-tool-admin chart to point the
// ingress controller to serve the newest release
func Reroute(context *Context) error {
	if err := os.Chdir(context.ChartDir); err != nil {
		return err
	}
	valuesName := "values.yaml"
	currentName := context.OldRelease + "-values.yaml"

	if ok := fileExists(&valuesName); ok {
		backup(valuesName, currentName, true)
		alterWatch(currentName, valuesName, context.NewRelease, true)
	} else {
		log.Fatal("values.yaml not found")
	}

	helmLint(".")
	if listErr := helmList(context.OldRelease, context.AdminNamespace); listErr != nil {
		return listErr
	}
	if listErr := helmList(context.NewRelease, context.CoreNamespace); listErr != nil {
		return listErr
	}

	fmt.Printf("Upgrading old release %s\n", context.OldRelease)
	result, _ := exec.Command("helm3", "upgrade", context.OldRelease, ".").Output()
	if checkErr(result) {
		log.Fatal(string(result))
	}
	return nil
}
