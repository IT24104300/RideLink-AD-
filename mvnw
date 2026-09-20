#!/bin/sh
# RideLink Maven Wrapper — downloads Apache Maven 3.9.9 on first use.
set -e
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
MAVEN_VERSION=3.9.9
DIST_NAME="apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${HOME}/.m2/wrapper/dists/${DIST_NAME}"
URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/${DIST_NAME}-bin.zip"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  echo "Downloading Maven ${MAVEN_VERSION} (first run only)..."
  TMPDIR="${TMPDIR:-/tmp}/ridelink-mvn-wrap"
  rm -rf "${TMPDIR}"
  mkdir -p "${TMPDIR}" "${HOME}/.m2/wrapper/dists"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "${URL}" -o "${TMPDIR}/${DIST_NAME}-bin.zip"
  else
    wget -q "${URL}" -O "${TMPDIR}/${DIST_NAME}-bin.zip"
  fi
  unzip -q "${TMPDIR}/${DIST_NAME}-bin.zip" -d "${TMPDIR}/out"
  mv "${TMPDIR}/out/${DIST_NAME}" "${MAVEN_HOME}"
  rm -rf "${TMPDIR}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
