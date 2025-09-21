# betting-server
I have implemented a high-performance betting system backend service based on Java's built-in HttpServer. The main design ideas are as follows:

1. Data structure design
Use ConcurrentHashMap to store session and betting data to ensure thread safety.

Session management: Use two Maps to achieve bidirectional lookup (customer ID → session, session → customer ID)

Betting data storage: Nested Map structure (Betting plan ID → Customer ID → Maximum betting amount)

2. Session Management
The session validity period is 10 minutes. Expired sessions are regularly cleared by background threads.

Session key generation uses a secure random string generation algorithm.

Session verification is conducted through the sessionkey in the query parameters.

3. Performance Optimization
All data structures use concurrent-safe versions.

Use an efficient data sorting algorithm (Quick Sort)

Avoid unnecessary object creation and memory allocation.

4. Error Handling
Return appropriate HTTP status codes for invalid requests.

Verify the parameter format for errors.

How to Run
Make sure Java 17 and Maven are installed.

Run the following command in the root directory of the project: mvn clean package

After the build is completed, a file named betting-server-1.0.0.jar will be generated in the target directory.

To run the project, execute the following command:
'java -jar target/betting-server-1.0.0.jar'

The service will start at localhost:8001.
