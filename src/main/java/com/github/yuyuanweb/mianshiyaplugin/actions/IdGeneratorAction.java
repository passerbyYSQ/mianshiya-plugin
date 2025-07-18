package com.github.yuyuanweb.mianshiyaplugin.actions;

import cn.hutool.core.util.IdUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class IdGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Project project = event.getProject();
        IdTypeManager typeManager = IdTypeManager.getInstance();
        
        // 弹出选择菜单
        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(
                    Arrays.asList(IdType.UUID.getValue(), IdType.SnowflakeID.getValue())
            )
            .setSelectedValue(typeManager.getLastUsedType(), true)
            .setTitle("Select ID Type")
            .setItemChosenCallback(selected -> {
                typeManager.setLastUsedType(selected); // 持久化选择
                String id = selected.equals(IdType.UUID.getValue()) ? generateUuid() : generateSnowflakeId();
                insertText(editor, project, id);
            })
            .createPopup()
            .showInBestPositionFor(editor);
    }

    private String generateSnowflakeId() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    private String generateUuid() {
        return IdUtil.randomUUID();
    }

    private void insertText(Editor editor, Project project, String text) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            Document document = editor.getDocument();
            Caret caret = editor.getCaretModel().getCurrentCaret();
            // 在光标处插入
            document.insertString(caret.getOffset(), text);
            // 移动光标到UUID末尾
            caret.moveToOffset(caret.getOffset() + text.length());
        });
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // 仅在编辑器激活时启用
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setEnabledAndVisible(editor != null);
    }
}