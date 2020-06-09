#!/bin/bash -eu

if [ ! -f ~/bin/bazel ]; then
curl -fsSLO https://github.com/bazelbuild/bazel/releases/download/3.2.0/bazel-3.2.0-installer-linux-x86_64.sh
chmod 755 bazel-3.2.0-installer-linux-x86_64.sh
./bazel-3.2.0-installer-linux-x86_64.sh --user
else
  echo "Bazel already present"
fi
