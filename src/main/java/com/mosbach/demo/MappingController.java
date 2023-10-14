package com.mosbach.demo;

import com.mosbach.demo.data.api.SortOrder;
import com.mosbach.demo.data.api.TaskManager;
import com.mosbach.demo.data.api.UserManager;
import com.mosbach.demo.data.impl.*;
import com.mosbach.demo.model.auth.EmailToken;
import com.mosbach.demo.model.alexa.AlexaRO;
import com.mosbach.demo.model.alexa.OutputSpeechRO;
import com.mosbach.demo.model.alexa.ResponseRO;
import com.mosbach.demo.model.auth.OnlyToken;
import com.mosbach.demo.model.auth.SendBackToken;
import com.mosbach.demo.model.auth.User;
import com.mosbach.demo.model.student.Student;
import com.mosbach.demo.model.student.StudentList;
import com.mosbach.demo.model.student.StudentNoPassword;
import com.mosbach.demo.model.task.Task;
import com.mosbach.demo.model.task.TaskList;
import com.mosbach.demo.model.task.TokenTask;
import com.mosbach.demo.model.task.TokenTaskid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1.0")
public class MappingController {

    // Turn on if you store data to postgres
    // UserManager userManager = PostgresDBUserManagerImpl.getPostgresDBUserManagerImpl();

    // Turn on if you store data to property files
    UserManager userManager = PropertyFileUserManagerImpl.getPropertyFileUserManagerImpl("src/main/resources/users.properties");
    // TaskManager taskManager = PropertyFileTaskManagerImpl.getPropertyFileTaskManagerImpl("src/main/resources/tasks.properties");
    TaskManager taskManager = PostgresDBTaskManagerImpl.getPostgresDBUserManagerImpl();


    /**
     * GET /auth only for testing whether the server is alive
     */
    @GetMapping("/auth")
    public String getInfo(@RequestParam(value = "name", defaultValue = "Student") String name) {
        Logger.getLogger("MappingController").log(Level.INFO,"MappingController auth " + name);
        return "ok";
    }

    @PostMapping(
            path = "/auth/login",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public SendBackToken signIn(@RequestBody User user) {

        // todo Check user credentials are ok

        return
                new SendBackToken("12345", 12);

    }


    /**
     * GET /tasks to get all tasks
     */
    @GetMapping("/tasks/all")
    public TaskList getAllTasks(@RequestParam(value = "sortOrder", defaultValue = "date") String sortOrder,
                              @RequestParam(value = "token", defaultValue = "no-token") String token) {
        Logger.getLogger("MappingController")
                .log(Level.INFO,"MappingController /tasks/all " + sortOrder);

        // Step 1: Check token

        // Step 2: Fetch all tasks from DB
        List<com.mosbach.demo.data.api.Task> tasksFromFile = taskManager.readAllTasks();
        List<Task> mytasks = new ArrayList<>();
        for (com.mosbach.demo.data.api.Task t : tasksFromFile)
            mytasks.add(new Task(t.getName(), t.getPriority()));

        return
                new TaskList(mytasks);
    }

    /**
     * POST /tasks einen neuen Task anlegen
     */
    @PostMapping(
            path = "/tasks",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public String addTask(@RequestBody TokenTask tokenTask) {

        Logger.getLogger("MappingController").log(Level.INFO,"MappingController POST /tasks "
                + tokenTask.getTask().getName());

        // Step 1: Check token

        // Step 3: Add task to database
        taskManager.addTask(tokenTask.getTask().getName(), tokenTask.getTask().getPriority());

        return "We will add the following task " + tokenTask.getTask().getName();
    }

    @PostMapping(
            path = "/alexa",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public AlexaRO sendTasksToAlexa(@RequestBody AlexaRO alexaRO) {

        Logger.getLogger("MappingController").log(Level.INFO,"MappingController POST /alexa ");
        String outText = "";

        if (alexaRO.getRequest().getType().equalsIgnoreCase("LaunchRequest"))
            outText += "Welcome to the Mosbach Task Organizer. ";

        if (alexaRO.getRequest().getType().equalsIgnoreCase("IntentRequest")
            &&
            (alexaRO.getRequest().getIntent().getName().equalsIgnoreCase("TaskReadIntent"))
        ) {
            outText += "You have to do the following tasks. ";
            List<com.mosbach.demo.data.api.Task> tasks = taskManager.readAllTasks();
            int i = 1;
            for (com.mosbach.demo.data.api.Task t : tasks) {
                outText += "Task Number " + i + " : " + t.getName()
                        + " with priority " + t.getPriority() + " . ";
                i++;
            }
        }

        return
                prepareResponse(alexaRO, outText, true);
    }


    private AlexaRO prepareResponse(AlexaRO alexaRO, String outText, boolean shouldEndSession) {

        alexaRO.setRequest(null);
        alexaRO.setContext(null);
        alexaRO.setSession(null);
        OutputSpeechRO outputSpeechRO = new OutputSpeechRO();
        outputSpeechRO.setType("PlainText");
        outputSpeechRO.setText(outText);
        ResponseRO response = new ResponseRO(outputSpeechRO, shouldEndSession);
        alexaRO.setResponse(response);
        return alexaRO;
    }



    @GetMapping("/create-task-table")
    public String createDBTable(@RequestParam(value = "token", defaultValue = "no-token") String token) {
        Logger.getLogger("MappingController")
                .log(Level.INFO,"MappingController create-task-table " + token);

        // Check token

        taskManager.createTaskTable();

        return "ok";
    }


    @PostMapping(
        path = "/auth/register",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
@ResponseStatus(HttpStatus.CREATED)
public SendBackToken registerUser(@RequestBody User user) {
    
    // Check if the user with the given email already exists
    if (userManager.getEmailForToken(user.getUserEmail()).equals("not-found")) {
        com.mosbach.demo.data.api.User createdUser = userManager.createUser(user.getUserName(), user.getPassword(), user.getUserEmail());
        
        if (createdUser != null) {
            // You can generate a token for the user and send it back or just send a succes s message
            return new SendBackToken("12345", 12); // Replace '12345' with the actual token
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create user");
        }
    } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with the given email already exists");
    }
}

}


