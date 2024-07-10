import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { StaffDetailComponent } from './staff-detail.component';

describe('Staff Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StaffDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: StaffDetailComponent,
              resolve: { staff: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(StaffDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load staff on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', StaffDetailComponent);

      // THEN
      expect(instance.staff).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
