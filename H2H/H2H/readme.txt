Run the build.xml.
Copy the folder h2h to the required destination.
Navigate inside the folder h2h.

Run the application by using-
Windows-
java -cp "./h2h.jar;./lib/logback.xml;./lib/*" org.stb.main.Main

Unix-
java -cp "./h2h.jar:./lib/logback.xml:./lib/*" org.stb.main.Main



For debugging with ConsoleClient-
Windows-
java -cp "./lib/org.hive2hive.client-1.2.3-SNAPSHOT.jar;./lib/logback.xml;./lib/*" org.hive2hive.client.ConsoleClient

Unix-
java -cp "./lib/org.hive2hive.client-1.2.3-SNAPSHOT.jar:./lib/logback.xml:./lib/*" org.hive2hive.client.ConsoleClient
