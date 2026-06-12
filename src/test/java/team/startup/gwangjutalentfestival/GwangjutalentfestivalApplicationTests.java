package team.startup.gwangjutalentfestival;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class GwangjutalentfestivalApplicationTests {

	@MockBean
	Sheets sheets;

	@MockBean
	Drive drive;

	@Test
	void contextLoads() {
	}

}
