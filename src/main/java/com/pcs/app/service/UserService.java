package com.pcs.app.service;

import com.pcs.app.domain.User;
import com.pcs.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository repository;

    @Autowired
    private ApplicationContext context;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad credentials"));
    }

    public List<User> getAllUsers(){
        return repository.findAll();
    }

    public User getUserById(int userId){
        return repository.findById(userId).orElseThrow();
    }

    public User createUser(User user) {
        return repository.save(user);
    }

    public User updateUser(User user){
        if (user.getId() == null || !repository.existsById(user.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id");
        }
        return repository.save(user);
    }

    public void deleteUser(int userId) {
        if (!repository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No user with given id");
        }
        repository.deleteById(userId);
    }
}
