set -x

UpdateParserFiles=../
WORKSPACE_DIR=$( readlink -f ../.. )
JDT_PLUGIN_DIR=$WORKSPACE_DIR/org.eclipse.jdt.core_3.6.0_ohl

cp $UpdateParserFiles/*.rsc  $JDT_PLUGIN_DIR/src/org/eclipse/jdt/internal/compiler/parser

cp $UpdateParserFiles/readableNames.properties $JDT_PLUGIN_DIR/src/org/eclipse/jdt/internal/compiler/parser
