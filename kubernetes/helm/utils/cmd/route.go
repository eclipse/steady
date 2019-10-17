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
	"github.com/spf13/cobra"
)

// routeCmd represents the route command
var routeCmd = &cobra.Command{
	Use:   "route",
	Short: "reconfigures the admin chart",
	Long: `This subcommand allows for upgrading the vulnerability assessment tool
admin chart hosted on k8s to serve the latest release. In short, this upgrade
the ingresses (not the entire chart along with the NGINX Ingress controller) allowing
for a ~zero downtime upgrade.`,
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		chartDir := args[0]

		if oldRelease == "" {
			log.Fatal("Admin release name required")
		}

		if newRelease == "" {
			log.Fatal("Core release name required")
		}

		promote.Reroute(&promote.Context{
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

func init() {
	rootCmd.AddCommand(routeCmd)

	// Here you will define your flags and configuration settings.
	routeCmd.PersistentFlags().StringVar(&adminNamespace, "adminNamespace", "", "admin namespace")
	routeCmd.PersistentFlags().StringVar(&coreNamespace, "coreNamespace", "", "core namespace")
	routeCmd.PersistentFlags().StringVar(&oldRelease, "adminRelease", "", "current admin release name")
	routeCmd.PersistentFlags().StringVar(&newRelease, "coreRelease", "", "current core chart release name")
}
