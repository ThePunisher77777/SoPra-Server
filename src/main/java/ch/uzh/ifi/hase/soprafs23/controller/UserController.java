package ch.uzh.ifi.hase.soprafs23.controller;

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
    public ResponseEntity<UserPostDTO> createUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        User createdUser = userService.createUser(userInput);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", createdUser.getToken());

        return new ResponseEntity<>(DTOMapper.INSTANCE.convertEntityToUserPostDTO(createdUser), headers, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> getUser(@RequestParam long userId, @RequestHeader("token") String token) {
        userService.canUserBeAuthorizedByToken(token);

        User user = userService.getUser(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("token", user.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));

    }

    @PutMapping ("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity updateUser(@RequestHeader("token") String token, @RequestBody UserPutDTO userPutDTO) {
        userService.canUserBeAuthorizedByToken(token);

        User userToBeUpdated = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);

        userService.updateUser(userToBeUpdated, token);

        return ResponseEntity.noContent().build();

    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginGetDTO> loginUser(@RequestBody LoginPostDTO loginPostDTO) {
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
    public ResponseEntity<LogoutPostDTO> logoutUser(@RequestHeader("token") String token) {

        User loggedOutUser = userService.logoutUser(token);

        return ResponseEntity.ok()
                .body(DTOMapper.INSTANCE.convertEntityToLogoutPostDTO(loggedOutUser));

    }
}