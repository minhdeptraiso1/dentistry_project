-- =========================
-- PUBLIC CONTENTS
-- =========================
CREATE TABLE public_contents (
                                 id UUID PRIMARY KEY,

                                 ref_id UUID,
                                 ref_type VARCHAR(50) NOT NULL,

                                 slug VARCHAR(255),

                                 title VARCHAR(255) NOT NULL,
                                 subtitle VARCHAR(255),

                                 description TEXT,
                                 content TEXT,

                                 image_url TEXT,
                                 sub_image_urls TEXT,
                                 thumbnail_url TEXT,

                                 extra_data JSONB,

                                 active BOOLEAN NOT NULL DEFAULT TRUE,
                                 featured BOOLEAN NOT NULL DEFAULT FALSE,
                                 sort_order INT NOT NULL DEFAULT 0,

                                 created_at TIMESTAMP,
                                 updated_at TIMESTAMP,
                                 created_by VARCHAR(100),
                                 updated_by VARCHAR(100),
                                 deleted_at TIMESTAMP,
                                 deleted_by VARCHAR(100),

                                 CONSTRAINT chk_public_contents_ref_type
                                     CHECK (ref_type IN ('DOCTOR', 'SERVICE', 'MEDICINE'))
);

CREATE INDEX idx_pc_ref ON public_contents(ref_id, ref_type);
CREATE INDEX idx_pc_type ON public_contents(ref_type);
CREATE INDEX idx_pc_active ON public_contents(active);
CREATE INDEX idx_pc_featured ON public_contents(featured);
CREATE INDEX idx_pc_sort_order ON public_contents(sort_order);

CREATE UNIQUE INDEX uq_pc_slug
    ON public_contents(slug)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_pc_ref_unique
    ON public_contents(ref_id, ref_type)
    WHERE ref_id IS NOT NULL AND deleted_at IS NULL;