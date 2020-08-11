package release

import (
	"math/rand"
	"time"
)

var (
	names = [...]string{
		"archimedes",
		"austin",
		"banzai",
		"beaver",
		"blackwell",
		"bohr",
		"cerf",
		"cohen",
		"feynman",
		"hamilton",
	}
)

func init() {
	rand.Seed(time.Now().UTC().UnixNano())
}

func generateReleaseName() *string {
	return &names[rand.Intn(len(names))]
}

// GenerateNonConflictRelease generates a releasename different from the old one
func GenerateNonConflictRelease(oldRelease *string) *string {
	for {
		name := generateReleaseName()
		if name != oldRelease {
			return name
		}
	}
}
