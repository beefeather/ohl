echo Generating parser files

#set -x

WORKSPACE_DIR=$( readlink -f ../.. )

JIKES_OUTPUT=../var/jikesps-output

JIKES_EXE=$( readlink -f $WORKSPACE_DIR/org.eclipse.jdt.proxy.project/jikespg.exe )


JDT_PLUGIN_DIR=$WORKSPACE_DIR/org.eclipse.jdt.core_3.6.0_ohl

UpdateParserFiles=../

mkdir -p $JIKES_OUTPUT

pushd $JIKES_OUTPUT

$JIKES_EXE $JDT_PLUGIN_DIR/java.g  > log 2>&1 
grep "This grammar is LALR" log
GREP_RES=$?
if [ $GREP_RES -ne 0 ]; then echo Not LALR ; exit ; fi

popd

PATCH_OUTPUT=../var/patch-output

mkdir -p $PATCH_OUTPUT

cp patch_tmpl/ParserBasicInformation.java.before \
  $PATCH_OUTPUT/ParserBasicInformation.java.tmp

merge $PATCH_OUTPUT/ParserBasicInformation.java.tmp patch_tmpl/javadef.java.before $JIKES_OUTPUT/javadef.java

sed -e "s/};/}/" $PATCH_OUTPUT/ParserBasicInformation.java.tmp > $PATCH_OUTPUT/ParserBasicInformation.java

rm $PATCH_OUTPUT/ParserBasicInformation.java.tmp

cp patch_tmpl/TerminalTokens.java.before $PATCH_OUTPUT/TerminalTokens.java
merge $PATCH_OUTPUT/TerminalTokens.java patch_tmpl/javasym.java.before $JIKES_OUTPUT/javasym.java


cp $JIKES_OUTPUT/javadcl.java $JIKES_OUTPUT/javahdr.java $UpdateParserFiles

cp $PATCH_OUTPUT/ParserBasicInformation.java $PATCH_OUTPUT/TerminalTokens.java $JDT_PLUGIN_DIR/src/org/eclipse/jdt/internal/compiler/parser

echo Now manually patch Parser.java with $JIKES_OUTPUT/JavaAction.java 
echo Run UpdateParserFile
echo Then call step2.sh