package app.chat;

import app.chat.service.MapChangeListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private final MapChangeListenerService mapChangeListenerService;

	@Autowired
	public DemoApplication(MapChangeListenerService mapChangeListenerService) {
		this.mapChangeListenerService = mapChangeListenerService;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		mapChangeListenerService.startMapChangeListener();
	}
}
