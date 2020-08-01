package moe.gensoukyo.rpgmaths.common.stats;

import moe.gensoukyo.rpgmaths.RpgMathsMod;
import moe.gensoukyo.rpgmaths.api.stats.AbstractStatType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 将一个Attribute作为实现方式的RPG属性
 * @see IAttribute
 * @author Chloe_koopa
 */
public class AttributeBackendStat
        extends AbstractStatType
{
    @Nullable
    private IAttribute backend;

    static final Set<AttributeBackendStat> STATS_WITH_CUSTOM_ATTRIBUTE = new LinkedHashSet<>();

    /**
     * 指定一个attribute作为后端实现
     * @param backend 作为后端实现的Attribute
     */
    public AttributeBackendStat(@Nullable IAttribute backend)
    {
        this.backend = backend;
    }

    /**
     * 新建一个attribute作为后端实现
     */
    public AttributeBackendStat()
    {
        this(null);
        STATS_WITH_CUSTOM_ATTRIBUTE.add(this);
    }

    protected static final String ATTR_NAME_PATTERN = RpgMathsMod.ID + ".stat.%s";
    /**
     * 获取后端Attribute。
     * 在必要的时候初始化
     * @return 后端Attribute
     * @throws IllegalStateException 如果属性需要初始化，且在对象拥有注册名前初始化时throw
     */
    @Nonnull
    protected IAttribute getBackend()
    {
        if (this.backend == null)
        {
            if (this.getRegistryName() == null)
            {
                throw new IllegalStateException("Initializing before registering!");
            }
            this.backend = new RangedAttribute(
                    null,
                    String.format(ATTR_NAME_PATTERN, getRegistryName().toString().
                            replace(":",".")),
                    0d, -Double.MAX_VALUE, Double.MAX_VALUE
            );
        }
        return this.backend;
    }

    @Override
    public float getBaseValue(ICapabilityProvider owner)
    {
        if (owner instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) owner;
            return (float) living.getAttribute(this.getBackend()).getBaseValue();
        }
        return 0f;
    }

    @Override
    public boolean setBaseValue(ICapabilityProvider owner, float value)
    {
        if (owner instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) owner;
            living.getAttribute(this.getBackend()).setBaseValue(value);
            return true;
        }
        return false;
    }

    @Override
    public float getFinalValue(ICapabilityProvider owner)
    {
        if (owner instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) owner;
            return (float) living.getAttribute(this.getBackend()).getValue();
        }
        return super.getFinalValue(owner);
    }

    @Override
    public ITextComponent getDescription() {
        return null;
    }

    /**
     * 该对象基于引用判断相等
     */
    @Override
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}