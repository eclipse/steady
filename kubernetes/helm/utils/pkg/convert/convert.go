package convert

// Int32Ptr converts an int32 to an int32 pointer
func Int32Ptr(i int32) *int32 { return &i }

// Int64Ptr converts an int64 to an int64 pointer
func Int64Ptr(i int64) *int64 { return &i }

// BoolPtr converts a bool to a boolean pointer
func BoolPtr(b bool) *bool { return &b }
