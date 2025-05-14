// at the top of the file...

// inside ObjectManager

public void checkSpikesTouched(Player p) {
    for (Spike s : spikes) {
        if (s.getHitbox().intersects(p.getHitbox())) {
            p.kill();
        }
    }
}

public void checkObjectTouched(Rectangle2D.Float hitbox) {
    for (Potion p : potions) {
        if (p.isActive()) {
            if (hitbox.intersects(p.getHitbox())) {
                p.setActive(false);
                applyEffectToPlayer(p);
            }
        }
    }
}

public void applyEffectToPlayer(Potion p) {
    if (p.getObjType() == RED_POTION)
        playing.getPlayer().changeHealth(RED_POTION_VALUE);
    else
        playing.getPlayer().changeHealth(BLUE_POTION_VALUE);
}

public void checkObjectHit(Rectangle2D.Float attackbox) {
    for (GameContainer gc : containers) {
        if (gc.isActive() && !gc.doAnimation) {
            if (gc.getHitbox().intersects(attackbox)) {
                gc.setAnimation(true);
                int type = 0;
                if (gc.getObjType() == BARREL)
                    type = 1;
                potions.add(new Potion(
                        (int) (gc.getHitbox().x + gc.getHitbox().width / 2),
                        (int) (gc.getHitbox().y - gc.getHitbox().height / 2),
                        type));
                return;
            }
        }
    }
}

public void loadObjects(Level newLevel) {
    potions = new ArrayList<>(newLevel.getPotions());
    containers = new ArrayList<>(newLevel.getContainers());
    spikes = newLevel.getSpikes();
    cannons = newLevel.getCannons();
    projectiles.clear();
}

public void update(int[][] lvlData, Player player) {
    for (Potion p : potions) {
        if (p.isActive())
            p.update();
    }

    for (GameContainer gc : containers) {
        if (gc.isActive())
            gc.update();
    }

    updateCannons(lvlData, player);
    updateProjectiles(lvlData, player);
}

private boolean isPlayerInRange(Cannon c, Player player) {
    int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
    return absValue <= utilz.Game.TILES_SIZE * 5;
}

private boolean isPlayerInfrontOfCannon(Cannon c, Player player) {
    if (c.getObjType() == CANNON_LEFT) {
        if (c.getHitbox().x > player.getHitbox().x)
            return true;
    } else if (c.getObjType() == CANNON_RIGHT) {
        if (c.getHitbox().x < player.getHitbox().x)
            return true;
    }
    return false;
}

private void updateCannons(int[][] lvlData, Player player) {
    for (Cannon c : cannons) {
        if (!c.doAnimation) {
            if (c.getTileY() == player.getTileY()) {
                if (isPlayerInRange(c, player)) {
                    if (isPlayerInfrontOfCannon(c, player)) {
                        if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY())) {
                            c.setAnimation(true);
                        }
                    }
                }
            }
        }

        c.update();
        if (c.getAniIndex() == 4 && c.getAniTick() == 0) {
            shootCannon(c);
        }
    }
}

private void shootCannon(Cannon c) {
    int dir = 1;
    if (c.getObjType() == CANNON_LEFT)
        dir = -1;
    projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
}

public void draw(Graphics g, int xLvlOffset) {
    drawPotions(g, xLvlOffset);
    drawContainers(g, xLvlOffset);
    drawTraps(g, xLvlOffset);
    drawCannons(g, xLvlOffset);
    drawProjectiles(g, xLvlOffset);
}

public void resetAllObjects() {
    loadObjects(playing.getLevelManager().getCurrentLevel());

    for (Potion p : potions)
        p.reset();
    for (GameContainer gc : containers)
        gc.reset();
    for (Cannon c : cannons)
        c.reset();
}
