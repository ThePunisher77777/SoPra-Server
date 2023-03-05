package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User getUser(Long userId) {
        if (this.userRepository.findById(userId).isPresent()) {
            return this.userRepository.findById(userId).get();
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }
    }

    public void canUserBeAuthorizedByToken(String token) {
        if (this.userRepository.findByToken(token).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access token");
        }
    }

    public User loginUser(User userToLogin) {
        User userByUsername = userRepository.findByUsername(userToLogin.getUsername());

        if (userByUsername == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        if (userByUsername.getPassword().equals(userToLogin.getPassword())) {
            userByUsername.setStatus(UserStatus.ONLINE);
            return userByUsername;
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username or password incorrect");
        }
    }

    public User logoutUser(String token) {
        if (userRepository.findByToken(token).isPresent()) {
            User userToBeLoggedOut = userRepository.findByToken(token).get();
            userToBeLoggedOut.setStatus(UserStatus.OFFLINE);
            return userToBeLoggedOut;
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }
    }

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreationDate(new Date());
        checkIfUserExists(newUser);
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public void updateUser(User updateUser, String token) {
        if (userRepository.findByToken(token).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User not found user"));
        }
        else {
            User updatedUser = userRepository.findByToken(token).get();

            if (updateUser.getUsername() == null || updateUser.getUsername().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username can't be empty");
            }

            checkIfUsernameIsAlreadyTaken(updateUser, updatedUser);

            updatedUser.setUsername(updateUser.getUsername());
            updatedUser.setBirthday(updateUser.getBirthday());

            // saves the given entity but data is only persisted in the database once
            // flush() is called
            userRepository.save(updatedUser);
            userRepository.flush();

            log.debug("Created Information for User: {}", updateUser);
        }
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
        User userByName = userRepository.findByName(userToBeCreated.getName());

        String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
        if (userByUsername != null && userByName != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage, "username and the name", "are"));
        }
        else if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
        }
        else if (userByName != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "name", "is"));
        }
    }

    private void checkIfUsernameIsAlreadyTaken(User userToBeUpdated, User userOriginally) {
        User userByUsername = userRepository.findByUsername(userToBeUpdated.getUsername());

        if (userByUsername != null) {
            if (userByUsername.getUsername().equals(userOriginally.getUsername())) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }
    }
}
