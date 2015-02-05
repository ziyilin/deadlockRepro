package thread.builder;

public class TestThreadBuilder {
	public static void main(String[] args){
		testCase1();

		testCase2();
	}
	private static void testCase1(){
		ThreadBuilder.apply("src/main/resources/fulldump");		
	}
	private static void testCase2(){
		ThreadBuilder.apply("src/main/resources/dump");		
	}

}
