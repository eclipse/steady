// Package promote provides a module to promote a pod from the vulnerability-assessment-tool-core
// chart into a new release in order to allow for a migration with schema changes
package promote

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"

	"gopkg.in/yaml.v3"
)

// Context passed onto the promotion subcommand
type Context struct {
	OldRelease     string `yaml:"oldRelease"`
	NewRelease     string `yaml:"newRelease"`
	ChartDir       string `yaml:"ChartDir"`
	CoreNamespace  string `yaml:"coreNamespace"`
	AdminNamespace string `yaml:"coreNamespace"`
	Kubeconfig     string `yaml:"kubeconfig"`
	DryRun         bool   `yaml:"dryRun"`
}

func fileExists(name *string) bool {
	if _, err := os.Stat(*name); err == nil {
		return true
	}
	return false
}

func backup(src, dst string, overwrite bool) error {
	fmt.Printf("Backing up file %s into %s\n", src, dst)
	srcFile, readErr := ioutil.ReadFile(src)
	if readErr != nil {
		return readErr
	}

	if _, err := os.Stat(dst); err == nil {
		if !overwrite {
			return fmt.Errorf("promote.backup : cannot overwrite file")
		}
	}
	if writeErr := ioutil.WriteFile(dst, srcFile, 0644); writeErr != nil {
		return writeErr
	}
	return nil
}

func alterValues(src, dst, claimName string, overwrite bool) error {
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
		existingClaim := global.(map[string]interface{})["existingClaim"]
		if !overwrite && existingClaim != nil {
			return fmt.Errorf("promote.Copy : existing value cannot be overwritten")
		}
		global.(map[string]interface{})["existingClaim"] = claimName
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

func replaceFiles(context *Context, claimName string) {
	valuesName := "values.yaml"
	currentName := context.OldRelease + "-values.yaml"
	futureName := context.NewRelease + "-values.yaml"
	if ok := fileExists(&valuesName); ok {
		backup(valuesName, currentName, true)
		backup(valuesName, futureName, true)
		alterValues(futureName, valuesName, claimName, true)
	} else {
		log.Fatal("values.yaml not found")
	}
}
