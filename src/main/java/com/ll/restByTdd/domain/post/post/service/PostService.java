package com.ll.restByTdd.domain.post.post.service;

import com.ll.restByTdd.domain.member.member.entity.Member;
import com.ll.restByTdd.domain.post.post.entity.Post;
import com.ll.restByTdd.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    // 갯수 카운트
    public long count() {
        return postRepository.count();
    }

    // 작성
    public Post write(Member author, String title, String content, boolean published, boolean listed) {
        Post post = Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .published(published)
                .listed(listed)
                .build();

        return postRepository.save(post);
    }

    // 조회
    public List<Post> findAllByOrderByIdDesc() {
        return postRepository.findAllByOrderByIdDesc();
    }

    // 1개 찾기
    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public void modify(Post post, String title, String content, boolean published, boolean listed) {
        post.setTitle(title);
        post.setContent(content);
        post.setPublished(published);
        post.setListed(listed);
    }

    public Optional<Post> findLaTest() { // 최신꺼 하나 내놔
        return postRepository.findFirstByOrderByIdDesc();
    }

    public void flush() {
        postRepository.flush();
    }

    public List<Post> findByListedPaged(boolean listed, int page, int pageSize) {
        // JPA에서 제공하는 라이브러리
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("published"));

        Page<Post> postPage = postRepository.findByListed(listed, pageRequest);

        return postPage.getContent();
    }
}
