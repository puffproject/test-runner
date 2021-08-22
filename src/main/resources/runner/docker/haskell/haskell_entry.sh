#!/bin/bash

# # Set virtual memory limit (Should be big) (in kbytes)
ulimit -v $PF_VIR_MEM
ulimit -Hv $PF_VIR_MEM

# # Set stack memory (in kbytes)
ulimit -s $PF_STK_MEM
ulimit -Hs $PF_STK_MEM

# # Set max size of files written by the shell (for temp storage for pytest) (in kbytes)
ulimit -f $PF_MAX_FILE_SZ
ulimit -Hf $PF_MAX_FILE_SZ

# Set max memory size (in kbytes) (Not respected for alpine)
ulimit -m $PF_MAX_MEM
ulimit -Hm $PF_MAX_MEM

# # Set maximum number of open file descriptors
ulimit -n $PF_MAX_FILE_DESCRIPTORS
ulimit -Hn $PF_MAX_FILE_DESCRIPTORS

# do the same command for the list of given dependencies.

# Build and run test file
timeout -s TERM $PF_TIMEOUT stack ghc "$TEST_FILE_NAME" -- -main-is $(basename "$TEST_FILE_NAME" .hs).main -o test

## Handling exit code for compilation command
COMPILESTATUS=$?
case $COMPILESTATUS in
    0) ## Successful compilation
        timeout -s TERM $PF_TIMEOUT ./test
        ;;
    1) ## Compilation failure
        exit 3
        ;;
    124) ## Command timed out
        exit 137
        ;;
    *) ## Some other failure
        echo "Some other error occurred"
        exit $COMPILESTATUS
        ;;
esac
