* What's this
This is a eclipse plugin for check if a method have a caller.
(Same with ctrl+alt+H in eclipse, but support multiple methods)


* Install

1). Copy CallCheck.jar to your eclipse/dropins
2). Restart eclipse, note that `CallChek` will show in your eclipse menu.


* Usage

1. Copy method qualified name from eclipse 
   or from execution result of:
      com.dmsum.mentimun.test.tool.ListAllDTOPublicMethods
      com.dmsum.mentimun.test.tool.ListAllDTOBuilderPublicBuildMethods

2. Click `CallChek` menu in eclipse
3. Paste qulified names in step 1
4. Press OK button
5. Check result will show in your eclipse console.


* What is reuslt mean

1*:	it means the seq num of your pasted method
[Y]: 	it means the method have the caller
[N]: 	it means the method do NOT have any caller

Sample result:
  1*[Y]	static void testCollect() [in TestMe [in [Working copy] TestMe.java [in aloha.test [in src [in Test]]]]]
  2*[N]	static void testCollect2() [in TestMe [in [Working copy] TestMe.java [in aloha.test [in src [in Test]]]]]