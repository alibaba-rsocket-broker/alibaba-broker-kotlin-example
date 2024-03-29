package com.alibaba.spring.boot.rsocket.demo;

import com.alibaba.rsocket.RSocketService;
import com.alibaba.rsocket.util.ByteBufTuples;
import com.alibaba.user.User;
import com.alibaba.user.UserService;
import net.datafaker.Faker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * user service implementation
 *
 * @author leijuan
 */
@RSocketService(serviceInterface = UserService.class)
@Service
public class UserServiceImpl implements UserService {
    private Faker faker = new Faker();

    @Override
    public Mono<User> findById(Integer id) {
        return Mono.just(randomUser(id));
    }

    @Override
    public Mono<User> findByEmailOrPhone(String email, String phone) {
        return Mono.fromCallable(() -> {
            User user = randomUser(null);
            user.setEmail(email);
            user.setPhone(phone);
            return user;
        });
    }

    @Override
    public Mono<User> _findByIdOrNick(ByteBuf byteBuf) {
        Tuple2<Integer, String> tuple2 = ByteBufTuples.of(byteBuf, Integer.class, String.class);
        User user = new User();
        user.setId(tuple2.getT1());
        user.setNick(tuple2.getT2());
        user.setEmail(faker.internet().emailAddress());
        user.setPhone(faker.phoneNumber().phoneNumber());
        return Mono.just(user);
    }

    @Override
    public Mono<List<User>> findAll() {
        return Mono.just(Arrays.asList(randomUser(null), randomUser(null)));
    }

    @Override
    public Mono<Integer> save(User user) {
        return Mono.just(new Random().nextInt());
    }

    @Override
    public Mono<Void> flush(String name) {
        System.out.println("Flush triggered by FNF");
        return Mono.empty();
    }

    @Override
    public Mono<String> getAppName() {
        return Mono.just("UserService");
    }

    @Override
    public Mono<Void> job1() {
        System.out.println("job1 triggered by FNF");
        return Mono.empty();
    }

    @Override
    public Flux<User> findAllPeople(String type) {
        return Flux.range(0, 10)
                .map(id -> new User(id, "nick:" + type));
    }

    @Override
    public Flux<User> recent(Flux<Integer> userIdFlux) {
        return userIdFlux
                .map(id -> new User(id, "nick:" + id));
    }

    @Override
    public Flux<User> recentWithType(String type, Flux<Integer> userIdFlux) {
        return userIdFlux
                .map(id -> new User(id, type + ":" + id));
    }

    @Override
    public Mono<String> error(String text) {
        return Mono.error(new Exception("this is an Exception!"));
    }

    @Override
    public Mono<ByteBuf> findAvatar(Integer id) {
        return Mono.just(Unpooled.wrappedBuffer("this is avatar".getBytes()));
    }

    @Override
    public Mono<ByteBuf> findUserByIdAndNick(ByteBuf byteBuf) {
        Tuple2<Integer, String> tuple2 = ByteBufTuples.of(byteBuf, Integer.class, String.class);
        //language=json
        String jsonData = "{\n" +
                "  \"id\": " + tuple2.getT1() + ",\n" +
                "  \"nick\": \"" + tuple2.getT2() + "\"\n" +
                "}";
        return Mono.just(Unpooled.wrappedBuffer(jsonData.getBytes(StandardCharsets.UTF_8)));
    }

    private User randomUser(@Nullable Integer id) {
        User user = new User();
        user.setId(id == null ? new Random().nextInt() : id);
        user.setNick(faker.name().name());
        user.setPhone(faker.phoneNumber().cellPhone());
        user.setEmail(faker.internet().emailAddress());
        return user;
    }
}
