# VA Computing Task
This is a sample app that communicates with a math engine service to perform some arithmetic (addition, subtraction, multiplication, and division between two operands). The math engine service is a background service and is responsible for executing the math equations entered by the user as scheduled tasks and notifying the app about the results.

### Important Implementation Details
1. The code is written in **Java** and relies on the java concurrent package for scheduling tasks in a thread pool, other implementations could rely on Rxjava or WorkManager libraries.
2. The project is **simply structured** but in a real project we should consider using some well known archetectures like (_MVVM_ or _MVP_).
3. The app UI is simple and represented by single *Activity*, but could be splitted it into multiple *Fragments*.
4. I am going to add another implementation using **Kotlin & coroutines**.
5. The background service will be restarted after being destroyed by the OS, and I am considering to add a mechanism to restart the service after device rebooting (we could use the Android AlarmManager API or by scheduling a periodic background job to check that the service is running and restart it if needed).

### Other Notes
* The project could be implemented using some Jetpack libraries like (Data Binding, ViewModel, LiveData, etc..).
* We could use a dependency injection framework like *Dagger*.
* I will continually add some small incremental improvements to the implementation.
