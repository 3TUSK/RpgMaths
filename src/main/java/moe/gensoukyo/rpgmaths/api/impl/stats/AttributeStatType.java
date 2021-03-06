package moe.gensoukyo.rpgmaths.api.impl.stats;

import moe.gensoukyo.rpgmaths.RpgMathsConfig;
import moe.gensoukyo.rpgmaths.RpgMathsMod;
import moe.gensoukyo.rpgmaths.client.attributes.StatTooltipHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 将一个Attribute作为实现方式的RPG属性。
 * 用此对象 构建/包装 的Attribute不会因玩家死亡而重置，
 * 并且会使用自己的命名。
 * @see IAttribute
 * @see moe.gensoukyo.rpgmaths.common.attributes.AdditionalAttributeHandler
 * @see StatTooltipHandler 自带的tooltip美化/重命名功能
 * @author Chloe_koopa
 */
public class AttributeStatType extends StoredStatType {
    @Nullable
    private IAttribute backend;

    public static final Set<AttributeStatType> STATS_WITH_CUSTOM_ATTRIBUTE = new LinkedHashSet<>();
    public static final Set<AttributeStatType> INSTANCES = new LinkedHashSet<>();
    protected static final Map<String, AttributeStatType> BY_ATTR_NAME = new HashMap<>();

    @Nullable
    public static AttributeStatType byAttributeName(String attribute) {
        return BY_ATTR_NAME.getOrDefault(attribute, null);
    }

    /**
     * 指定一个attribute作为后端实现
     *
     * @param backend 作为后端实现的Attribute
     */
    public AttributeStatType(@Nullable IAttribute backend) {
        this.backend = backend;
        if (backend != null) {
            if (RpgMathsConfig.FIX_ATTRIBUTE.get() && (backend instanceof RangedAttribute)) {
                ((RangedAttribute) backend).minimumValue = -Double.MAX_VALUE;
                ((RangedAttribute) backend).maximumValue = Double.MAX_VALUE;
            }
            putBackend();
        }
        INSTANCES.add(this);
    }

    /**
     * 新建一个attribute作为后端实现
     */
    public AttributeStatType() {
        this(null);
        STATS_WITH_CUSTOM_ATTRIBUTE.add(this);
    }

    protected static final String ATTR_NAME_PATTERN = RpgMathsMod.ID + ".stat.%s";

    /**
     * 获取后端Attribute。
     * 在必要的时候初始化
     *
     * @return 后端Attribute
     * @throws IllegalStateException 如果属性需要初始化，且在对象拥有注册名前初始化时throw
     */
    @Nonnull
    public IAttribute getBackend() {
        if (this.backend == null) {
            if (this.getRegistryName() == null) {
                throw new IllegalStateException("Initializing before registering!");
            }
            this.backend = new RangedAttribute(
                    null,
                    String.format(ATTR_NAME_PATTERN, getRegistryName().toString().
                            replace(":", ".")),
                    0d, -Double.MAX_VALUE, Double.MAX_VALUE
            );
            putBackend();
        }
        return this.backend;
    }

    private void putBackend() {
        BY_ATTR_NAME.put(this.getBackend().getName(), this);
    }

    @Override
    public double getBaseValue(ICapabilityProvider owner) {
        if (owner instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) owner;
            return living.getAttribute(this.getBackend()).getBaseValue();
        }
        return super.getBaseValue(owner);
    }

    @Override
    public boolean setBaseValue(ICapabilityProvider owner, double value) {
        if (owner instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) owner;
            living.getAttribute(this.getBackend()).setBaseValue(value);
            return true;
        }
        return super.setBaseValue(owner, value);
    }

    @Override
    public double getFinalValue(ICapabilityProvider owner) {
        if (owner instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) owner;
            return living.getAttribute(this.getBackend()).getValue();
        }
        return super.getFinalValue(owner);
    }

    /**
     * @see moe.gensoukyo.rpgmaths.common.attributes.AdditionalAttributeHandler#onPlayerDied
     */
    public void storeToCap(ICapabilityProvider owner) {
        if (owner instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) owner;
            super.setBaseValue(living, living.getAttribute(this.getBackend()).getBaseValue());
        }
    }

    /**
     * @see moe.gensoukyo.rpgmaths.common.attributes.AdditionalAttributeHandler#onPlayerClone(PlayerEvent.Clone)
     */
    public void recoverFromCap(ICapabilityProvider owner, ICapabilityProvider old) {
        if (owner instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) owner;
            living.getAttribute(this.getBackend()).setBaseValue(super.getBaseValue(old));
        }
    }

    /**
     * 该对象基于引用判断相等
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "AttributeStatType{" +
                "backend=" + backend +
                ", order=" + order +
                '}';
    }
}