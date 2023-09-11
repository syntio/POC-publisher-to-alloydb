// The test executable.
package main

import "testing"

func TestHello(t *testing.T) {
	t.Parallel()

	got := Hello()
	want := "Hello, world"

	if got != want {
		t.Errorf("got %q want %q", got, want)
	}
}
