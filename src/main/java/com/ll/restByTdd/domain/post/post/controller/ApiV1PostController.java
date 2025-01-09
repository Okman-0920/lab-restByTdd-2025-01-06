package com.ll.restByTdd.domain.post.post.controller;

import com.ll.restByTdd.domain.member.member.entity.Member;
import com.ll.restByTdd.domain.post.post.dto.PostDto;
import com.ll.restByTdd.domain.post.post.entity.Post;
import com.ll.restByTdd.domain.post.post.service.PostService;
import com.ll.restByTdd.global.rq.Rq;
import com.ll.restByTdd.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping("/{id}")
    public PostDto item(@PathVariable long id) {
        Post post = postService.findById(id).get();

        if (!post.isPublished()) {
            Member actor = rq.checkAuthentication();

            post.checkActorCanRead(actor);
        }

        return new PostDto(post);
    }


    record postWriteReqBody (
            @NotBlank @Length(min = 2) String title,
            @NotBlank @Length(min = 2) String content,
    ) {
    }

    @PostMapping("/write")
    public RsData<PostDto> writeItem(
            @RequestBody @Valid postWriteReqBody reqBody
    ) {
        Member actor = rq.checkAuthentication();


        Post post = postService.write(actor, reqBody.title, reqBody.content, true, true);

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다".formatted(post.getId()),
                new PostDto(post));
    }

    record PostModifyReqBody (
            @NotBlank String title,
            @NotBlank String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostDto> modifyItem(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(id).get();

        post.checkActorCanModify(actor);

        postService.modify(post, reqBody.title, reqBody.content());

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 글 수정이 완료되었습니다".formatted(post.getId()),
                new PostDto(post)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteItem(
            @PathVariable long id
    ) {
        Member actor = rq.checkAuthentication();

        Post post = postService.findById(id).get();

        post.checkActorCanDelete(actor);

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(post.getId())
        );
    }
}
