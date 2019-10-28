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

	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/internal/promote"
	"github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils/pkg/release"
	"github.com/spf13/cobra"
)

// upgradeCmd represents the upgrade command
var upgradeCmd = &cobra.Command{
	Use:   "upgrade",
	Short: "Automates upgrades that require schema changes",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		chartDir := args[0]

		if oldRelease == "" {
			log.Fatal("old release required")
		}

		if newRelease == "" {
			newRelease = *release.GenerateNonConflictRelease(&oldRelease)
			log.Printf("new release name not provided, automatically generated (%s)", newRelease)
		}

		promote.HelmUpgrade(&promote.Context{
			OldRelease:     oldRelease,
			NewRelease:     newRelease,
			ChartDir:       chartDir,
			Kubeconfig:     kubeconfig,
			DryRun:         dryRun,
			CoreNamespace:  coreNamespace,
			AdminNamespace: adminNamespace,
		})

	},
}

var (
	oldRelease, newRelease, adminNamespace string
	dryRun                                 bool
)

func init() {
	rootCmd.AddCommand(upgradeCmd)

	// Here you will define your flags and configuration settings.
	upgradeCmd.PersistentFlags().StringVarP(&coreNamespace, "namespace", "n", "", "core namespace")
	upgradeCmd.PersistentFlags().StringVarP(&oldRelease, "oldrelease", "o", "", "old release name")
	upgradeCmd.PersistentFlags().StringVarP(&newRelease, "futurerelease", "f", "", "(optional) new release name")
	upgradeCmd.PersistentFlags().BoolVarP(&dryRun, "dryrun", "d", true, "plans the migration out")
}
