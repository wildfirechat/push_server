package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.admin.entity.AdminUser;
import cn.wildfirechat.push.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AdminInitService {
    private static final Logger LOG = LoggerFactory.getLogger(AdminInitService.class);

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (adminUserRepository.count() == 0) {
            String username = "admin";
            String password = "admin123";
            String secretKey = "push_server_default_secret_key_change_me";

            AdminUser user = new AdminUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setSecretKey(secretKey);
            adminUserRepository.save(user);
            LOG.info("Initialized default admin user: {}", user.getUsername());
            LOG.warn("============================================================================");
            LOG.warn("WARNING: Default admin password is '{}'. Please change it immediately!", password);
            LOG.warn("============================================================================");
        }
    }
}
