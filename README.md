# VA Computing Task
This is a sample app that communicates with a math engine service to perform some arithmetic (addition, subtraction, multiplication, and division between two operands). The math engine service is a background service and is responsible for executing the math equations entered by the user as scheduled tasks and notifying the app about the results.

### Important Implementation Details
1. The code is written in **Java** and relies on the WorkManager for scheduling tasks.
2. The project is **simply structured** but in a real project we should consider using some well known archetectures like (_MVVM_ or _MVP_).
3. The app UI is simple and represented by single *Activity*, but could be splitted it into multiple *Fragments*.
4. The background service will be restarted after being destroyed by the OS or after device boot.
5. Unit & Instrumentation Tests code is written in **Kotlin**.
6. I am going to add another implementation using **Kotlin**.

### Build & Installation Instructions
There are two gradle custom tasks that could be used to clean, build, and install the app, one for the QA flavor and the other for the Prod flavor
* On Windows, run the following commands in a terminal window:
	use `gradlew cleanBuildInstallQA` to install the QA version of the app.
	use `gradlew cleanBuildInstallProd` to install the Prod version of the app.
* On Linux or Mac, run the following commands in a terminal window:
	use `./gradlew cleanBuildInstallQA` to install the QA version of the app.
	use `./gradlew cleanBuildInstallProd` to install the Prod version of the app.

### Other Notes
* The project could be implemented using some Jetpack libraries like (Data Binding, ViewModel, LiveData, etc..).
* We could use a dependency injection framework like *Dagger*.
* I will continually add some small incremental improvements to the implementation.
