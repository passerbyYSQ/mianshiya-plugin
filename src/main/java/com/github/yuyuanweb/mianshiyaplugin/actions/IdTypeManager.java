package com.github.yuyuanweb.mianshiyaplugin.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "IdTypeManager",
        storages = @Storage("id-type-manager.xml")
)
public class IdTypeManager implements PersistentStateComponent<IdTypeManager.State> {
    private State state = new State();

    public static IdTypeManager getInstance() {
        return ApplicationManager.getApplication().getService(IdTypeManager.class);
    }

    // 存储结构
    public static class State {
        public String lastUsedIdType = IdType.UUID.getValue(); // 默认值
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    // 业务方法
    public String getLastUsedType() {
        return state.lastUsedIdType;
    }

    public void setLastUsedType(String type) {
        state.lastUsedIdType = type;
    }
}