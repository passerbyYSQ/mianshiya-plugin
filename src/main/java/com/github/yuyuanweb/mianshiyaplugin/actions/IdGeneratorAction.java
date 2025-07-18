package com.github.yuyuanweb.mianshiyaplugin.actions;

import cn.hutool.core.util.IdUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class IdGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
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

                SelectionModel selectionModel = editor.getSelectionModel();
                if (selectionModel.hasSelection()) {
                    replaceSelectedText(editor, id);
                } else {
                    insertText(editor, id);
                }
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

    private void replaceSelectedText(Editor editor, String text) {
        SelectionModel selectionModel = editor.getSelectionModel();
        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();
        // 在写操作中执行替换
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
            editor.getDocument().replaceString(start, end, text);
            // 选中新生成的ID（可选）
            selectionModel.setSelection(start, start + text.length());
        });
    }

    private void insertText(Editor editor, String text) {
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
            Caret caret = editor.getCaretModel().getCurrentCaret();
            // 在光标处插入
            editor.getDocument().insertString(caret.getOffset(), text);
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