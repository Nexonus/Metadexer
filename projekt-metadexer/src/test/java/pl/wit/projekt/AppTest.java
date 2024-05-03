package pl.wit.projekt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppTest {
	@Test
	public void TestExecution() {
		App app = new App();
		Assertions.assertEquals("Hello World!", app.test());
	}
	
}
