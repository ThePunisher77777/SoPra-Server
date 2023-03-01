package ch.uzh.ifi.hase.soprafs23.controller;

import antlr.StringUtils;
import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.LoginGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }


    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUser(@PathVariable long userId) {
        // fetch all users in the internal representation

        User user = userService.getUser(userId);

        // convert each user to the API representation
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<UserGetDTO> createUser(@RequestHeader("password") String password, @RequestBody UserPostDTO userPostDTO) {
        //UserPostDTO userPostDTO = new UserPostDTO();
//        userPostDTO.setUsername(username);
//        userPostDTO.setName(name);
//        userPostDTO.setBirthday(birthday);
        userPostDTO.setPassword(password);

        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        User createdUser = userService.createUser(userInput);
        // convert internal representation of user back to API
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", createdUser.getToken());
        headers.set("password", createdUser.getPassword());

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    //@ResponseBody
    public ResponseEntity<LoginGetDTO> loginUser(@RequestHeader("username") String username, @RequestHeader("password") String password) {
        LoginPostDTO loginPostDTO = new LoginPostDTO();
        loginPostDTO.setUsername(username);
        loginPostDTO.setPassword(password);
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertLoginPostDTOtoEntity(loginPostDTO);

        // create user
        //User loggedInUser =
        User loggedInUser = userService.loginUser(userInput);

        // convert internal representation of user back to API
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", loggedInUser.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(DTOMapper.INSTANCE.convertEntityToLoginGetDTO(loggedInUser));
    }

    // public boolean verifyToken()

}
