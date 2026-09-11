<!-- Mode grid, items supprimables -->
<GenesisList display="grid" closable @close="removeItem">
    <GenesisItem
        v-for="fw in frameworks"
        :key="fw.id"
        :label="fw.name"
        :sublabel="fw.type"
        :selected="fw.id === selectedId"
        @click="selectFramework(fw.id)"
        @close="removeFramework(fw.id)"
    />
</GenesisList>

<!-- Mode table -->
<GenesisList
    display="table"
    closable
    column-layout="1fr 1fr 80px 40px"
    :headers="[{ label: 'Nom' }, { label: 'Type' }, { label: 'Version' }]"
>
    <GenesisItem
        v-for="fw in frameworks"
        :key="fw.id"
        :selected="fw.id === selectedId"
        @click="selectFramework(fw.id)"
        @close="removeFramework(fw.id)"
    >
        <td class="p-3">{{ fw.name }}</td>
        <td class="p-3">{{ fw.type }}</td>
        <td class="p-3">{{ fw.version }}</td>
    </GenesisItem>
</GenesisList>