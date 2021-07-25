<h1 align="center">
  <img src="https://github.com/puffproject/docs/blob/master/logo.png" height="200"/><br>
  Puff
</h1>

<h4 align="center">Open source smoke testing platform</h4>

<!-- TODO Add link to platform >
<!-- <h4 align="center">Open source smoke testing platform | <a href="LINK" target="_blank">LINK</a></h4> -->

<p align="center">
  <a href="https://www.oracle.com/ca-en/java/technologies/javase/javase-jdk8-downloads.html" rel="nofollow"><img src="https://img.shields.io/badge/java-1.8-009ACD?style=flat-square&logo=Java" alt="java version" data-canonical-src="https://img.shields.io/badge/java-1.8-f39f37?style=flat-square&logo=Java" style="max-width:100%;"></a>
  <a href="https://spring.io/projects/spring-boot" rel="nofollow"><img src="https://img.shields.io/badge/spring--boot-3.2.0-6db33f?style=flat-square&logo=Spring" alt="spring boot version" data-canonical-src="https://img.shields.io/badge/spring--boot-3.2.0-6db33f?style=flat-square&logo=Spring" style="max-width:100%;"></a>
  <a href="https://swagger.io" rel="nofollow"><img src="https://img.shields.io/badge/swagger-2.0-6c9a00?style=flat-square&logo=Swagger" alt="swagger version" data-canonical-src="https://img.shields.io/badge/swagger-2.0-6c9a00?style=flat-square&logo=Swagger" style="max-width:100%;"></a>
</p>

<blockquote align="center">
  <em>Puff</em> is an open source smoke testing platform for students to collaboratively write and run tests on their assignment or project code for quick and easy sanity testing.
</blockquote>

# Test-runner

[![Build & Tests](https://img.shields.io/github/workflow/status/puffproject/test-runner/Build%20&%20Test?label=build%20%26%20tests)](https://github.com/puffproject/test-runner/actions/workflows/build.yml)
[![Code coverage](https://codecov.io/gh/puffproject/test-runner/branch/master/graph/badge.svg?token=YRCB9LTSNC)](https://codecov.io/gh/puffproject/test-runner)

Spring-boot microservice managing user code uploads and running test cases for the puff platform. For the full overview of the puff project see the [docs repository](https://github.com/puffproject/docs).

## Getting started
Clone the project with `https://github.com/puffproject/test-runner.git`

### Install Java
You'll need Java to run _Puff's_ microservices developed with [Spring Boot](https://spring.io/projects/spring-boot).

* Download and install the [Java JDK 8](https://www.oracle.com/ca-en/java/technologies/javase/javase-jdk8-downloads.html)
* Set the `JAVA_HOME` environment variable
* Verify your installation by running `java -version`

### Install Maven
_Puff_ uses [Maven](https://maven.apache.org/) as its build tool for its backend.
* [Download](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html) Maven
* Verify your installation with `mvn -v`

### Setup Keycloak (If you haven't already)
> Keycloak is an open source Identity and Access Management solution aimed at modern applications and services. It makes it easy to secure applications and services with little to no code.

_Puff_ uses keycloak as a user management and authentication solution. More information about Keycloak can be found on their [offical docs page](https://www.keycloak.org/docs/latest/index.html).

Follow the instructions found at https://github.com/puffproject/docs#setup-keycloak to setup a local keycloak server. This only needs to be configured once.

Once configured, generate an authentication token by making the following curl call **replacing TEST_USER_USERNAME**, **TEST_USER_PASSWORD** and **USER_AUTH_CLIENT_SECRET** with the credentials for the test accounts you created and the client-secret for user-auth.

```shell
curl -X POST 'http://localhost:8180/auth/realms/puff/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'grant_type=password' \
 --data-urlencode 'client_id=user-auth' \
 --data-urlencode 'client_secret=USER_AUTH_CLIENT_SECRET' \
 --data-urlencode 'username=TEST_USER_USERNAME' \
 --data-urlencode 'password=TEST_USER_PASSWORD'
```

### Install Docker
_Puff_ uses [Docker](https://www.docker.com/) containers to isolate and run test cases with user code in a secure environment, before returning their result.

* Install Docker from their [Getting started](https://www.docker.com/get-started) page.
* Build the following base images so Docker can cache some of the layers used by the processes and exponentially speed up image build times.

Python3
```shell
cd src/main/resources/runner/docker/base && docker build -t pf_python3:base -f Dockerfile_py .
```
Once the images are built go back to the root of the repository for the rest of the steps.

* Change the `runner.dir` value in `src/main/resources/application-local.yml` to point to a directory where _Puff_ can copy code files and run containers from.

### Run the backend
In order to run a microservice locally run from the root directory:
```shell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
* If you need to build the `.jar` of the application run `mvn package`.
* In order to run tests run `mvn test`.


### Running tests
See the below commands for running tests:
* run all tests: `mvn test`
* run specific tests in a class: `mvn test -Dtest=TestClassName`
* run specific method in a class: `mvn test -Dtest=TestClassName#TestMethodName`
* run tests and generate a coverage report: `mvn test -P coverage`

## Development

### Swagger
_Puff_'s Spring-Boot backend exposes a REST API. The project utilizes [Swagger](https://swagger.io/) to document and keep a consistent REST interface.

Once you have a microservice running (See [run the backend](#run-the-backend)) visit http://localhost:8083/swagger-ui.html. A `json` api version to be consumed and used to generate client libraries can be accessed at http://localhost:8083/v2/api-docs. 

Select `Authorize` and login with a test user account to try out any of the endpoints.

### H2 Database
_Puff_'s Spring-boot backend uses a H2 runtime database to simulate a database connection for local development. Once the project is running it can be accessed at http://localhost:8083/h2.

The credentials for the database are as follows:
```
Driver Class: org.h2.Driver
JDBC URL: jdbc:h2:mem:testdb
User Name: admin
Password:
```

For more information about H2 databases see the [H2 Database Engine](https://www.h2database.com/html/main.html).

### Formatting

The codebase is auto-formatted with the [formatter-maven-plugin](https://code.revelc.net/formatter-maven-plugin/) that will format all source code files in the `src/` and `test/` directories according to the settings in the [style.xml](style.xml) file, which are based on eclipse profile settings.

Run the `mvn formatter:format` command to run the formatter. It is also bound to the `format` goal that will run as part of the `compile` phase. 

You can also add the git [pre-commit](.hooks/pre-commit) hook to your local `.git/hooks` folder to run the formatter on pre-commit.

### Docker

_Puff_ runs each test case in isolation with a multi-layered, zero-trust approach to ensure security and speed when dealing with user submitted code. It accomplishes this with the following steps:

1. Building an image of the system on which to run the test case. The command is as follows:

```shell
docker build --build-arg DIR=/code -t image_name:tag .
```

The image is built on a base image for the programming language required. A new user `appuser` is provisioned and added to proper groups. All user code files required are copied from the working directory into the image. An entrypoint that will run the shell script containing the test command is configured. 

The following controls are applied: 

* Docker image build timeout with Java thread timeout

2. Running the constructed image as a container in which a shell script executing the test command is run. The command is as follows:
```shell
docker run --rm -m 450M --name my_container --env-file .env -e OTHER_ENV_VALUE=XX -v path/to/test/file:/code/file:ro image_name
```

The container is run as an executable with an environment supplied through an environment file. The container's entrypoint is the shell script containing the test command to run the test case. The test suite file containing the test cases to run is supplied through a read-only [volume](https://docs.docker.com/storage/volumes/) which is modified by _Puff_ for each test case. 

The following controls are applied:

* Docker memory limit with the `-m` flag
* Virtual memory limit with `ulimit -v`
* Stack memory limit with `ulimit -s`
* Max user processes with `ulimit -u`
* Max file size written with `ulimit -f`
* Max number of file descriptors open with `ulimit -n`
* Test command timeout with `timeout`
* Docker container timeout with Java thread timeout

Most of the above values are configurable from the environment file or the profile.

## Supported Languages

### Python 3
_Puff_ supports projects written in python 3 and uses [pytest](https://docs.pytest.org/en/6.2.x/) for running test cases. After defining a test suite set the suite file following the below template:
```python
from pytest import *    # Python import REQUIRED

# ... import all source modules and function
# ... import any other helper libraries

## Define a test class (or don't and use simple test functions)
## Name MUST start with Test* for it to be recognized
class TestFunc:

	def setup_method(self, method):
		# Setup any values test cases should have access to
		# e.g. self.x = 2
		pass

	def teardown_method(self, method):
		# Teardown any values setup
		pass
	
	## There are other methods that pytest can take advantage of
	# For more information see https://docs.pytest.org/en/6.2.x/getting-started.html#create-your-first-test

	## All other test cases defined with be appended here
	# ...
```

Users will then upload test cases as part of the suite. Once users upload source files they can run those test cases using your suite file against their uploaded source code.

For a full demo see the [sample walkthrough](#sample-walkthrough) below.

## Sample walkthrough
If you've followed the above setup steps then you should be able to follow the below walkthrough. This example uses test cases written in python.

Start up the microservice, see [run the backend](#run-the-backend). Authenticate as a user by making the curl call described in [setup keycloak](#Setup-Keycloak-(if-you-haven't-already)). Copy your token and set it as an environment variable:
```shell
TOKEN=YOUR_TOKEN_HERE
```
To pretty format the curl requests I'm using python's json tooling. If you don't have [python](https://www.python.org/downloads/) remove the pipe at the end of the curl call.

You then need to create a **test suite** that will contain **test cases**. Make the following curl call:
```shell
curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request POST \
  --data '{"assignmentId": 1000, "language": "PYTHON3", "name": "Tests my function"}' \
  http://localhost:8083/suite | python -m json.tool
```
Make a note of the id returned and set it as a variable `SUITE_ID=...`. See the list of test suites by running
```shell
curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request GET \
  http://localhost:8083/suite | python -m json.tool
```

Next, set the test suite's _base file_, all test case code will be appended to this file so it needs to be setup correctly. Create a file `test_func.py` with the following contents:

```python
from pytest import *
from Func import *

## Test class for some function
class TestFunc:

	def setup_method(self, method):
		self.x = 2

	def teardown_method(self, method):
		pass
```

Run the follow curl command to set the file for the test suite.

```shell
curl --header "Content-Type: multipart/form-data" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request POST \
  -F file=@test_func.py \
  http://localhost:8083/suite/${SUITE_ID}/setFile | python -m json.tool
```

Next, create a `Func.py` file with a single function:
```python
def getX():
	return 2
```

Upload your `Func.py` file as the "source code" for your project you want to test:
```shell
curl --header "Content-Type: multipart/form-data" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request POST \
  -F files=@Func.py \
  -F assignmentId=1000 \
  http://localhost:8083/upload | python -m json.tool
```

Next upload the code for a test case you want to run. We'll check that the value returned from `getX()` in `Func.py` is equal to 2. Our test case will look like
```python
def test_isTwo(self):
	assert self.x == getX()
```

 Create the test case in the test suite with the following call:

```shell
curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request POST \
  --data "{\"description\": \"Tests if value of x is 2\", \"body\": \"assert self.x == getX()\", \"suiteId\": ${SUITE_ID}, \"language\": \"PYTHON3\", \"functionName\": \"isTwo\"}" \
  http://localhost:8083/case | python -m json.tool
```

Make a note of the id of the case and set it as a variable `CASE_ID=...`.

Lastly, run the test case, specifying the id of the test case to run:
```shell
curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${TOKEN}" \
  --request POST \
  --no-buffer \
  http://localhost:8083/suite/${SUITE_ID}/run?ids=${CASE_ID} | python -m json.tool
```

You should receive a json response matching something like the following:
```
{
	"caseId": {CASE_ID},
	"status": "PASS",
	"message": ...
}
```

## Contributors
The _Puff_ project is looking for contributors to join the initiative!
For information about progress, features under construction and opportunities to contribute see [our project board](https://github.com/benjaminkostiuk/unity-test/projects/1).


If you're interested in helping please read our [CONTRIBUTING.md](./CONTRIBUTING.md) for details like our Code of Conduct and contact [Benjamin Kostiuk](mailto:benkostiuk1@gmail.com) for more information.