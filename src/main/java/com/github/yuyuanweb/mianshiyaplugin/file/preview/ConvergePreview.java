package com.github.yuyuanweb.mianshiyaplugin.file.preview;

import com.github.yuyuanweb.mianshiyaplugin.constant.ViewConstant;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * 聚合多个编辑器 文件编辑器
 *
 * @author pine
 */
public class ConvergePreview extends UserDataHolderBase implements TextEditor {

    private static final Logger logger = Logger.getInstance(ConvergePreview.class);

    private final Project project;
    private final FileEditor[] fileEditors;
    private final String[] names;
    private final TabInfo[] tabInfos;
    private final VirtualFile file;
    private final Document document;
    private final Editor myEditor;


    private JComponent myComponent;
    private JBEditorTabs jbEditorTabs;

    public ConvergePreview(@NotNull FileEditor[] fileEditors, String[] names, Project project, VirtualFile file) {
        this.project = project;
        this.fileEditors = fileEditors;
        this.names = names;
        this.tabInfos = new TabInfo[names.length];
        this.file = file;
        logger.warn("mianshiya log before create document");
        document = FileDocumentManager.getInstance().getDocument(file);
        logger.warn("mianshiya log after create document: " + document);

        // 如果没有现有的编辑器，创建一个新的 EditorImpl 实例
        myEditor = EditorFactory.getInstance().createEditor(document, project);
        logger.warn("mianshiya log after create myEditor: " + myEditor);
    }

    @Override
    public @NotNull JComponent getComponent() {
        if (myComponent == null) {
            jbEditorTabs = new JBEditorTabs(project, IdeFocusManager.getInstance(project), this);

            // 创建第一个面板
            JComponent firstEditorComponent = fileEditors[0].getComponent();
            BorderLayoutPanel firstComponent = JBUI.Panels.simplePanel(firstEditorComponent);

            // 为第二和第三个内容创建标签
            for (int i = 1; i < fileEditors.length; i++) {
                TabInfo tabInfo = new TabInfo(fileEditors[i].getComponent());
                tabInfo.setText(names[i]);
                tabInfos[i] = tabInfo;
                jbEditorTabs.addTab(tabInfo);
            }

            jbEditorTabs.addListener(new TabsListener() {
                @Override
                public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                    // 从 1 开始，因为 0 已经被上部分使用
                    for (int i = 1; i < names.length; i++) {
                        if (newSelection.getText().equals(names[i])) {
                            fileEditors[i].setState(TabFileEditorState.TabFileEditorLoadState);
                            break;
                        }
                    }
                }
            });

            // 使用 Splitter 来分隔上下部分
            // false 表示垂直分隔，0.2f 表示初始比例
            JFrame frame = WindowManager.getInstance().getFrame(project);
            float proportion = 0;
            if (frame != null) {
                int height = frame.getHeight();
                // 处理获取的高度
                proportion = 200.0f / height;
            } else {
                proportion = 0.2f;
            }
            Splitter splitter = new Splitter(true, proportion);
            splitter.setFirstComponent(firstComponent);
            splitter.setSecondComponent(jbEditorTabs);

            myComponent = JBUI.Panels.simplePanel(splitter);
        }
        return myComponent;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return fileEditors[0].getPreferredFocusedComponent();
    }

    @Override
    public @NotNull String getName() {
        return ViewConstant.CONVERGE_PREVIEW;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        if (state instanceof TabSelectFileEditorState) {
            if (jbEditorTabs != null) {
                String name = ((TabSelectFileEditorState) state).getName();
                for (int i = 0; i < names.length; i++) {
                    if (name.equals(names[i])) {
                        fileEditors[i].setState(state);
                        jbEditorTabs.select(tabInfos[i], true);
                    }
                }
            }
        }
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return fileEditors[0].getCurrentLocation();
    }

    @Override
    public void dispose() {
        for (FileEditor fileEditor : fileEditors) {
            Disposer.dispose(fileEditor);
        }
        if (myEditor != null) {
            EditorFactory.getInstance().releaseEditor(myEditor);
        }
    }


    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }

    @Override
    public Editor getEditor() {
        logger.warn("mianshiya log myEditor " + myEditor);
        return myEditor;
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {

    }

    @Data
    @AllArgsConstructor
    public static class TabFileEditorState implements FileEditorState {

        private boolean load;

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

        public static TabFileEditorState TabFileEditorLoadState = new TabFileEditorState(true);

    }

    @Getter
    public static class TabSelectFileEditorState implements FileEditorState {

        private String name;

        private String childrenState;

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

    }

}
