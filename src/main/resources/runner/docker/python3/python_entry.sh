#!/bin/sh

# Set virtual memory limit (Should be big) (in kbytes)
ulimit -v $PF_VIR_MEM
ulimit -Hv $PF_VIR_MEM

# Set stack memory (in kbytes)
ulimit -s $PF_STK_MEM
ulimit -Hs $PF_STK_MEM

# Set max user processes
ulimit -u $PF_MAX_USR
ulimit -Hu $PF_MAX_USR

# Set max size of files written by the shell (for temp storage for pytest) (in kbytes)
ulimit -f $PF_MAX_FILE_SZ
ulimit -Hf $PF_MAX_FILE_SZ

# Set max memory size (in kbytes) (Not respected for alpine)
ulimit -m $PF_MAX_MEM
ulimit -Hm $PF_MAX_MEM

# Set maximum number of open file descriptors
ulimit -n $PF_MAX_FILE_DESCRIPTORS
ulimit -Hn $PF_MAX_FILE_DESCRIPTORS

# Run pytest on test suite with timeout
timeout -s TERM $PF_TIMEOUT pytest -p no:cacheprovider "$TEST_FILE_NAME"