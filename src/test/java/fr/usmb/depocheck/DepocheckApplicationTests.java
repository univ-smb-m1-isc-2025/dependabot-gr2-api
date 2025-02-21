package fr.usmb.depocheck;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DepocheckApplicationTests {

	@Test
	void testCase() {
		Integer a = 10;
		Integer b = 20;
		try {
			Integer result = a + b;
			Assertions.assertEquals(30, result);
		}catch (Exception e) {
			Assertions.fail("Test case failed: " + e.getMessage());
		}
	}

}
