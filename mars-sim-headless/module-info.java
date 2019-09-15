module mars-sim-headless {

	requires java.io.File;
	requires java.io.BufferedReader;
	requires java.io.IOException;
	requires java.io.InputStream;
	requires java.io.InputStreamReader;
	requires java.io.IOException;
	
	requires java.net.URISyntaxException;
	requires java.text.DateFormat;
	requires java.lang.Runnable;
	requires java.time.LocalDateTime;
	
	requires java.util.Arrays;
	requires java.util.List;
			
	requires java.util.logging;
	
	requires junit;

    requires mars-sim-console;
    
	exports mars-sim-headless;
}