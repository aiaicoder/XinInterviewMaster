package com.xin.MianshiTong.model.dto.comment;

import com.xin.MianshiTong.model.entity.Comment;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 15712
 */
@Data
public class CommentAddRequest implements Serializable {
    /**
     * 父级评论
     */
    private Comment parentComment;

    /**
     * 当前评论
     */
    private Comment currentComment;
}
