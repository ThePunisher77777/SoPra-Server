package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<UserGetDTO>> getAllUsers(@RequestHeader("token") String token) {
        userService.canUserBeAuthorizedByToken(token);

        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }

        return ResponseEntity.ok()
                .body(userGetDTOs);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<UserGetDTO> createUser(@RequestHeader("password") String password, @RequestBody UserPostDTO userPostDTO) {
        userPostDTO.setPassword(password);

        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        User createdUser = userService.createUser(userInput);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", createdUser.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser));
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> getUser(@PathVariable long userId, @RequestHeader("token") String token) {
        userService.canUserBeAuthorizedByToken(token);

        User user = userService.getUser(userId);
        user.setToken(token);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", user.getToken());

        System.out.println("TOKEN" + headers.get("token"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));

//        return ResponseEntity.ok().body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));

    }

    @PutMapping ("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> editUser(@PathVariable long userId, @RequestHeader("token") String token) {
        userService.canUserBeAuthorizedByToken(token);

        User user = userService.getUser(userId);

        return ResponseEntity.ok().body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));

    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginGetDTO> loginUser(@RequestHeader("username") String username, @RequestHeader("password") String password) {
        LoginPostDTO loginPostDTO = new LoginPostDTO();
        loginPostDTO.setUsername(username);
        loginPostDTO.setPassword(password);

        User userInput = DTOMapper.INSTANCE.convertLoginPostDTOtoEntity(loginPostDTO);

        User loggedInUser = userService.loginUser(userInput);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", loggedInUser.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToLoginGetDTO(loggedInUser));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logoutUser(@RequestHeader("token") String token) {

        User userToBeLoggedOut = userService.logoutUser(token);

        userToBeLoggedOut.setStatus(UserStatus.OFFLINE);

    }
}
