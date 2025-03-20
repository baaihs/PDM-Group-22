# PDM-Group-22
1. Create credentials.txt in my-project/src/main/java/com/example. (Will be in location my-project/src/main/java/com/example/credentials.txt)
2. Add lines DB_USER=<YOUR_RIT_USERNAME> and 
             DB_PASSWORD=<YOUR_RIT_PASSWORD>
3. Download JavaPostgresSSHJars on mycourses(Project -> Additional files)
4. Drag jsch-0.1.55.jar and postgresql-42.2.24.jar into my-project (Will be in location my-project/jsch-0.1.55.jar and my-project/postgresql-42.2.24.jar)
5. Make sure you're in directory my-project
6. Execute in terminal: mvn compile
7. Execute in terminal: mvn exec:java -Dexec.mainClass="com.example.Database"