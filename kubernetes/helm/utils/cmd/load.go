/*
Copyright © 2019 ichbinfrog

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

// Package cmd provides a cli module to interact with utils
package cmd

import (
	"log"
	"os"

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/internal/load"
	"github.com/spf13/cobra"
)

// loadCmd represents the load command
var loadCmd = &cobra.Command{
	Use:   "load",
	Short: "command to populate vulnerability database",
	Long: `This subcommand allows for an user to load cve datas into the vulnerability
assessement tool database hosted inside a kubernetes cluster.

In short, in creates a number of concurrent go routines which launch and watch a series
of jobs, each in charge of loading a small chunk of the given bugs (thus allowing for more
efficient loading). As of this release, files must be a yaml file following this structure:

bugs:
- reference: { bug cve } ( vulnerability identifier )
	repo: { github repo } ( URL of the VCS repository hosting the library project )
	commit: { commit id } ( One or multiple revisions (multiple ones must be comma-separated w/o blanks).
													In the case of Git repositories, the revision can be optionally concatenated with )
	links: { optional links to cve ref } ( Comma-separated list of links to comprehensive vulnerability information
												(optional, it must be provided for vulnerabilities not available from the NVD) )
	description: { optional descriptions }
`,
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		sourceFile := args[0]
		if _, err := os.Stat(sourceFile); err != nil {
			log.Fatal(err)
		}

		// Concurrent amount check
		if concurrent < 1 {
			log.Fatalf("Concurrent values cannot be less then 1")
		}

		// Release name check
		if releaseName == "" {
			log.Fatalf("Release name cannot be empty")
		}

		// Namespace check
		if coreNamespace == "" {
			log.Fatalf("Namespace cannot be empty")
		}

		context := load.Context{
			Concurrent:  concurrent,
			Source:      sourceFile,
			ReleaseName: releaseName,
			Namespace:   coreNamespace,
			DryRun:      noUpload,
			Skip:        skipIfExist,
		}

		list, _ := load.SplitCVE(&context)
		load.UploadBugs(&context, list)
	},
}

var (
	releaseName           string
	concurrent            int
	skipIfExist, noUpload bool
)

func init() {
	rootCmd.AddCommand(loadCmd)

	// Here you will define your flags and configuration settings.
	loadCmd.PersistentFlags().StringVarP(&coreNamespace, "namespace", "n", "vulnerability-assessment-tool-core", "core namespace")
	loadCmd.PersistentFlags().IntVarP(&concurrent, "concurrent", "c", 5, "amount of parallel jobs handling the load")
	loadCmd.PersistentFlags().StringVarP(&releaseName, "release", "r", "canary", "release name of core chart")
	loadCmd.PersistentFlags().BoolVarP(&noUpload, "dry-run", "d", false, "does not upload to the backend")
	loadCmd.PersistentFlags().BoolVarP(&skipIfExist, "skip", "s", false, "skip if already exists")
}
