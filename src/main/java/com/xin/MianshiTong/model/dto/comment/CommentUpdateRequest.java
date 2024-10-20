package com.xin.MianshiTong.model.dto.comment;


import com.xin.MianshiTong.model.entity.Comment;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 15712
 */
@Data
public class CommentUpdateRequest implements Serializable {

    /**
     * 当前评论
     */
    private Comment currentComment;
//    private Long id;
//
//    /**
//     * 点赞数
//     */
//    private Integer likeCount;

}
