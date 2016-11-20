#!/bin/bash

# !!!!!!!!!!!!!!
# This file is outdated. It was introduced in previous versions
# of Yamcs Studio to build a complete offline development copy
# of all CSS p2 repos. This was a lengthy, fragile process.
#
# Nowadays, we instead fetch our dependencies from their update
# sites, which saves us building all the binaries.
# These update sites are configured in the *.target definition
# that can be found in one of the tycho maven modules.
#
# We keep it around for future reference, in case we need to
# build all CSS deps without relying on their update site
# (could be useful for working with unreleased versions too).
# !!!!!!!!!!!!!!

# Builds CSS from the sources using a local composite p2 repository as described
# in their documentation.

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

# Generate settings.xml (to be reused in Eclipse)
COMPOSITE_P2_REPO=$PRGDIR/composite-repository
mkdir -p $COMPOSITE_P2_REPO
COMPOSITE_P2_REPO_FULL=`cd "$COMPOSITE_P2_REPO"; pwd`
sed "s#REPLACE_WITH_COMPOSITE_P2_REPO#"$COMPOSITE_P2_REPO_FULL"#" $PRGDIR/css/settings_template.xml >$PRGDIR/css/settings.xml

# Verify that the css-for-yamcs-v2 maven profile actually exists
# We unfortunately need this external file. To make it even
# worse, this needs to have an absolute path in it. Blame maven.

# This command is sometimes a bit slow. So give some indication
echo "Searching for css-for-yamcs-v2 maven profile...."
if ! mvn help:all-profiles | grep css-for-yamcs-v2 >/dev/null
then
	# The user could have some other profiles in there. Don't just overwrite it
	if [ -f ~/.m2/settings.xml ]
	then
		echo 'Could not find css-for-yamcs-v2 maven profile. But ~/.m2/settings.xml'
		echo 'already exists. Merge its content with this snippet, then try again:'
		echo
		cat $PRGDIR/css/settings.xml
        echo
        echo 'If you have an old profile configured (i.e. css-for-yamcs),'
        echo 'replace its content with the above instead.'
		exit 1
	else
		echo 'Could not find css-for-yamcs-v2 maven profile.'
		read -p 'Generate one and copy it to ~/.m2/settings.xml now? [y/N]' yn
		case $yn in
			[Yy]* )
				cp $PRGDIR/css/settings.xml ~/.m2/settings.xml
				;;
			* )
				echo "Cannot continue without the css-for-yamcs-v2 maven profile"
				exit 1
		esac
	fi
fi

export MAVEN_OPTS="-Xmx2048m -Xss128M -XX:+CMSClassUnloadingEnabled -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"

set -e
mvn -DskipTests -f $PRGDIR/css/diirt/pom.xml -s $PRGDIR/css/settings.xml -Pcss-for-yamcs-v2 clean verify
mvn -DskipTests -f $PRGDIR/css/maven-osgi-bundles/pom.xml -s $PRGDIR/css/settings.xml -Pcss-for-yamcs-v2 clean verify
mvn -DskipTests -f $PRGDIR/css/cs-studio-thirdparty/pom.xml -s $PRGDIR/css/settings.xml -Pcss-for-yamcs-v2 clean verify
mvn -DskipTests -f $PRGDIR/css/cs-studio/core/pom.xml -s $PRGDIR/css/settings.xml -Pcss-for-yamcs-v2 clean verify
mvn -DskipTests -f $PRGDIR/css/cs-studio/applications/pom.xml -s $PRGDIR/css/settings.xml -Pcss-for-yamcs-v2 clean verify
set +e

echo
echo '----------------------------------------------------------------'
echo 'CS-Studio dependencies successfully built.'
echo '----------------------------------------------------------------'